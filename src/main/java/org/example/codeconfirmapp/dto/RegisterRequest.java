package org.example.codeconfirmapp.dto;

public record RegisterRequest(
        String username,
        String password,
        String role,
        String email,
        String phone,
        String telegramChatId
) {
}
