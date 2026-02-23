package com.hotel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    // Hash a plain-text password using SHA-256
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Verify plain-text password against stored hash
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        return hash(rawPassword).equals(storedHash);
    }

    // Validate password strength
    // Rules: min 6 chars, at least 1 digit
    public static void validateStrength(String password) {
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters");
        if (!password.matches(".*\\d.*"))
            throw new IllegalArgumentException(
                    "Password must contain at least one digit");
        if (password.contains(" "))
            throw new IllegalArgumentException(
                    "Password must not contain spaces");
    }

    // Utility: generate a hash to paste into MySQL for default admin
    // Run this once to get the hash for "Admin@123"
    public static void main(String[] args) {
        String[] passwords = {"Admin@123", "Staff@123"};
        for (String p : passwords)
            System.out.println("Password: " + p + " → Hash: " + hash(p));
    }
}