package org.example.codeconfirmapp.model;

import java.util.UUID;

public record User(
        UUID id,
        String username,
        String passwordHash,
        Role role,
        String email,
        String phone,
        String telegramChatId
) {
}
