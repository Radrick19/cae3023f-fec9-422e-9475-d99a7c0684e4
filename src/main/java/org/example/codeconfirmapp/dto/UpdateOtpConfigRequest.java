package org.example.codeconfirmapp.dto;

public record UpdateOtpConfigRequest(
        Integer codeLength,
        Integer ttlSeconds
) {
}
