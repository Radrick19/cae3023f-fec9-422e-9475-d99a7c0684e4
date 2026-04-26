package org.example.codeconfirmapp.dto;

public record GenerateOtpRequest(
        String operationId,
        String channel,
        String destination
) {
}
