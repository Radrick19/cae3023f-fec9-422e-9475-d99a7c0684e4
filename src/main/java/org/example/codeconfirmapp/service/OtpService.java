package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.dao.OtpCodeDao;
import org.example.codeconfirmapp.dao.OtpConfigDao;
import org.example.codeconfirmapp.dao.UserDao;
import org.example.codeconfirmapp.dto.GenerateOtpRequest;
import org.example.codeconfirmapp.dto.ValidateOtpRequest;
import org.example.codeconfirmapp.model.DeliveryChannel;
import org.example.codeconfirmapp.model.OtpCode;
import org.example.codeconfirmapp.model.OtpStatus;
import org.example.codeconfirmapp.model.User;
import org.example.codeconfirmapp.util.OtpCodeGenerator;
import org.example.codeconfirmapp.util.OtpCodeHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public OtpService(
            OtpCodeDao otpCodeDao,
            OtpConfigDao otpConfigDao,
            UserDao userDao,
            NotificationService notificationService
    ) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.userDao = userDao;
        this.notificationService = notificationService;
    }

    public Map<String, Object> generate(String username, GenerateOtpRequest request) throws SQLException {
        var user = getUserByUsername(username);

        if (request == null || isBlank(request.operationId()) || isBlank(request.channel())) {
            throw new IllegalArgumentException("Поля operationId и channel обязательны");
        }

        var config = otpConfigDao.getConfig();
        var code = OtpCodeGenerator.generateDigits(config.codeLength());
        var channel = DeliveryChannel.valueOf(request.channel().toUpperCase());
        var destination = resolveDestination(user, channel, request.destination());
        var now = Instant.now();
        var expiresAt = now.plusSeconds(config.ttlSeconds());

        var otpCode = new OtpCode(
                UUID.randomUUID(),
                user.id(),
                request.operationId(),
                OtpCodeHasher.hash(code),
                OtpStatus.ACTIVE,
                channel,
                destination,
                now,
                expiresAt
        );

        otpCodeDao.save(otpCode);
        notificationService.send(channel, destination, code);

        log.info("OTP сгенерирован: userId={}, operationId={}, channel={}, expiresAt={}",
                user.id(), request.operationId(), channel, expiresAt);

        return Map.of(
                "otpId", otpCode.id(),
                "operationId", request.operationId(),
                "channel", channel.name(),
                "destination", destination,
                "status", otpCode.status().name(),
                "expiresAt", expiresAt.toString()
        );
    }

    public Map<String, Object> validate(String username, ValidateOtpRequest request) throws SQLException {
        var user = getUserByUsername(username);

        if (request == null || isBlank(request.operationId()) || isBlank(request.code())) {
            throw new IllegalArgumentException("Поля operationId и code обязательны");
        }

        var otpCode = otpCodeDao.findLatestActiveByUserAndOperation(user.id(), request.operationId())
                .orElseThrow(() -> new IllegalArgumentException("Активный OTP-код не найден"));

        if (otpCode.expiresAt().isBefore(Instant.now()) || otpCode.expiresAt().equals(Instant.now())) {
            otpCodeDao.updateStatus(otpCode.id(), OtpStatus.EXPIRED.name());
            throw new IllegalArgumentException("Срок действия OTP-кода истек");
        }

        if (!OtpCodeHasher.hash(request.code()).equals(otpCode.codeHash())) {
            throw new IllegalArgumentException("OTP-код неверный");
        }

        otpCodeDao.updateStatus(otpCode.id(), OtpStatus.USED.name());

        log.info("OTP успешно подтвержден: userId={}, operationId={}, otpId={}",
                user.id(), otpCode.operationId(), otpCode.id());

        return Map.of(
                "operationId", otpCode.operationId(),
                "status", OtpStatus.USED.name(),
                "validatedAt", Instant.now().toString()
        );
    }

    public int expireOutdated() throws SQLException {
        return otpCodeDao.expireOlderThanNow();
    }

    private String resolveDestination(User user, DeliveryChannel channel, String requestDestination) {
        if (!isBlank(requestDestination)) {
            return requestDestination;
        }
        return switch (channel) {
            case EMAIL -> requireDestination(user.email(), "Не указан адрес электронной почты");
            case SMS -> requireDestination(user.phone(), "Не указан номер телефона");
            case TELEGRAM -> requireDestination(user.telegramChatId(), "Не указан идентификатор чата Telegram");
            case FILE -> "файл-в-корне-проекта";
        };
    }

    private String requireDestination(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private User getUserByUsername(String username) throws SQLException {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}
