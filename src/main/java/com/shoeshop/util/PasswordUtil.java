package com.shoeshop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Утилита хэширования паролей алгоритмом SHA-256. */
public final class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Возвращает SHA-256 хэш пароля в виде hex-строки (64 символа).
     * Использует UTF-8 кодировку входной строки.
     */
    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(64);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 гарантированно есть в JDK, это исключение не должно возникать
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }

    /** Сравнивает пароль с хэшем из базы данных. */
    public static boolean verify(String password, String storedHash) {
        return hash(password).equals(storedHash);
    }
}
