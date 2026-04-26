package org.example.codeconfirmapp.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        var salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        var hash = pbkdf2(rawPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        var salt = Base64.getDecoder().decode(parts[0]);
        var expected = Base64.getDecoder().decode(parts[1]);
        var actual = pbkdf2(rawPassword.toCharArray(), salt);
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            var spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось захешировать пароль", e);
        }
    }
}
