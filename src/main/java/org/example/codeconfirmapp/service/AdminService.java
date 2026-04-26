package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.dao.OtpCodeDao;
import org.example.codeconfirmapp.dao.OtpConfigDao;
import org.example.codeconfirmapp.dao.UserDao;
import org.example.codeconfirmapp.model.OtpConfig;
import org.example.codeconfirmapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserDao userDao;
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;

    public AdminService(UserDao userDao, OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao) {
        this.userDao = userDao;
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
    }

    public OtpConfig getConfig() throws SQLException {
        return otpConfigDao.getConfig();
    }

    public OtpConfig updateConfig(int codeLength, int ttlSeconds) throws SQLException {
        if (codeLength < 4 || codeLength > 10) {
            throw new IllegalArgumentException("Длина кода должна быть от 4 до 10 символов");
        }

        if (ttlSeconds < 30 || ttlSeconds > 3600) {
            throw new IllegalArgumentException("Время жизни кода должно быть от 30 до 3600 секунд");
        }

        var updated = otpConfigDao.updateConfig(new OtpConfig(codeLength, ttlSeconds));

        log.info("Конфигурация OTP обновлена: codeLength={}, ttlSeconds={}", updated.codeLength(), updated.ttlSeconds());

        return updated;
    }

    public List<Map<String, Object>> listUsers() throws SQLException {
        return userDao.findAllNonAdmins().stream()
                .map(this::toDto)
                .toList();
    }

    public boolean deleteUser(UUID userId) throws SQLException {
        otpCodeDao.deleteByUserId(userId);

        var deleted = userDao.deleteById(userId);

        log.info("Результат удаления пользователя: userId={}, deleted={}", userId, deleted);

        return deleted;
    }

    private Map<String, Object> toDto(User user) {
        return Map.of(
                "id", user.id(),
                "username", user.username(),
                "role", user.role().name(),
                "email", user.email() == null ? "" : user.email(),
                "phone", user.phone() == null ? "" : user.phone(),
                "telegramChatId", user.telegramChatId() == null ? "" : user.telegramChatId()
        );
    }
}
