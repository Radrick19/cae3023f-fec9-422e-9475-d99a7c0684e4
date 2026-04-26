package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.model.DeliveryChannel;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Component
public class FileNotificationSender implements NotificationSender {
    private final Path filePath = Path.of("otp-codes.txt");

    @Override
    public DeliveryChannel supports() {
        return DeliveryChannel.FILE;
    }

    @Override
    public void send(String destination, String code) {
        try {
            var line = Instant.now() + " | получатель=" + destination + " | код=" + code + System.lineSeparator();
            Files.writeString(filePath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось записать OTP-код в файл", e);
        }
    }
}
