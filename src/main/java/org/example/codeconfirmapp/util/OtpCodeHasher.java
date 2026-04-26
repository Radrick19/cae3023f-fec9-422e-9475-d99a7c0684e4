package org.example.codeconfirmapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class OtpCodeHasher {
    private OtpCodeHasher() {
    }

    public static String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось захешировать OTP-код", e);
        }
    }
}
