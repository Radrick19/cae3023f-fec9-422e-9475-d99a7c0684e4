package org.example.codeconfirmapp.dto;

public record ValidateOtpRequest(
        String operationId,
        String code
) {
}
