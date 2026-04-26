package org.example.codeconfirmapp.security;

import org.example.codeconfirmapp.model.Role;

import java.util.UUID;

public record AuthPrincipal(
        UUID userId,
        String username,
        Role role
) {
}
