package org.example.codeconfirmapp.controller;

import org.example.codeconfirmapp.dto.UpdateOtpConfigRequest;
import org.example.codeconfirmapp.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        log.info("Администратор запросил конфигурацию OTP");

        try {
            return ResponseEntity.ok(adminService.getConfig());
        } catch (SQLException e) {
            log.error("Ошибка базы данных при получении конфигурации OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateConfig(@RequestBody UpdateOtpConfigRequest request) {
        log.info("Администратор изменяет конфигурацию OTP: codeLength={}, ttlSeconds={}", request.codeLength(), request.ttlSeconds());

        try {
            if (request.codeLength() == null || request.ttlSeconds() == null) {
                throw new IllegalArgumentException("Поля codeLength и ttlSeconds обязательны");
            }

            return ResponseEntity.ok(adminService.updateConfig(request.codeLength(), request.ttlSeconds()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Ошибка базы данных при обновлении конфигурации OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        log.info("Администратор запросил список пользователей без роли администратора");

        try {
            return ResponseEntity.ok(adminService.listUsers());
        } catch (SQLException e) {
            log.error("Ошибка базы данных при получении списка пользователей", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        log.info("Администратор запросил удаление пользователя {}", userId);

        try {
            var deleted = adminService.deleteUser(userId);

            if (!deleted) {
                return ResponseEntity.status(404).body(Map.of("error", "Пользователь не найден"));
            }

            return ResponseEntity.ok(Map.of("deleted", true, "userId", userId));
        } catch (SQLException e) {
            log.error("Ошибка базы данных при удалении пользователя", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка базы данных"));
        }
    }
}
