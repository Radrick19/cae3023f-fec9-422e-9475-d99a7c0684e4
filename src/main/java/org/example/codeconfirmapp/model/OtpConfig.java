package org.example.codeconfirmapp.model;

public record OtpConfig(
        int codeLength,
        int ttlSeconds
) {
}
