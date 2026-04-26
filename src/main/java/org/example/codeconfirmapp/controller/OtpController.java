package org.example.codeconfirmapp.controller;

import org.example.codeconfirmapp.dto.GenerateOtpRequest;
import org.example.codeconfirmapp.dto.ValidateOtpRequest;
import org.example.codeconfirmapp.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateOtpRequest request, Authentication authentication) {
        log.info("Пользователь {} запросил генерацию OTP для операции {} через канал {}",
                authentication.getName(), request.operationId(), request.channel());

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(otpService.generate(authentication.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Ошибка базы данных при генерации OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ValidateOtpRequest request, Authentication authentication) {
        log.info("Пользователь {} запросил проверку OTP для операции {}",
                authentication.getName(), request.operationId());

        try {
            return ResponseEntity.ok(otpService.validate(authentication.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Ошибка базы данных при проверке OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }
}
