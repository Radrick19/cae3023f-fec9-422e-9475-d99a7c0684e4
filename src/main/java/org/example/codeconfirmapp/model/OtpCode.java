package org.example.codeconfirmapp.model;

import java.time.Instant;
import java.util.UUID;

public record OtpCode(
        UUID id,
        UUID userId,
        String operationId,
        String codeHash,
        OtpStatus status,
        DeliveryChannel deliveryChannel,
        String destination,
        Instant createdAt,
        Instant expiresAt
) {
}
