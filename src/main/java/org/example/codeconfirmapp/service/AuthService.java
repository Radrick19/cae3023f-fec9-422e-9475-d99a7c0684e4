package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.dao.UserDao;
import org.example.codeconfirmapp.dto.LoginRequest;
import org.example.codeconfirmapp.dto.RegisterRequest;
import org.example.codeconfirmapp.model.Role;
import org.example.codeconfirmapp.model.User;
import org.example.codeconfirmapp.security.AuthPrincipal;
import org.example.codeconfirmapp.security.JwtService;
import org.example.codeconfirmapp.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final JwtService jwtService;
    private final long jwtTtlSeconds;

    public AuthService(UserDao userDao, JwtService jwtService, @Value("${app.jwt.ttl-seconds:3600}") long jwtTtlSeconds) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    public Map<String, Object> register(RegisterRequest request) throws SQLException {
        validateRegisterRequest(request);

        var role = Role.valueOf(request.role().toUpperCase());

        if (role == Role.ADMIN && userDao.existsAdmin()) {
            throw new IllegalArgumentException("Администратор уже существует");
        }

        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        var user = new User(
                UUID.randomUUID(),
                request.username(),
                PasswordHasher.hash(request.password()),
                role,
                request.email(),
                request.phone(),
                request.telegramChatId()
        );

        userDao.save(user);

        log.info("Пользователь зарегистрирован: username={}, role={}", user.username(), user.role());

        return Map.of(
                "id", user.id(),
                "username", user.username(),
                "role", user.role().name()
        );
    }

    public Map<String, Object> login(LoginRequest request) throws SQLException {
        var user = userDao.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        if (!PasswordHasher.matches(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        var principal = new AuthPrincipal(user.id(), user.username(), user.role());

        log.info("Пользователь вошел в систему: username={}, role={}", user.username(), user.role());

        return Map.of(
                "token", jwtService.generateToken(principal),
                "expiresInSeconds", jwtTtlSeconds,
                "role", user.role().name()
        );
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password()) || isBlank(request.role())) {
            throw new IllegalArgumentException("Поля username, password и role обязательны");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
