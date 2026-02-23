package com.hotel.util;

public class ValidationUtil {

    // ─── String Checks ────────────────────────────────────────────────────

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " is required");
    }

    public static void requireLength(String value, String fieldName,
                                     int min, int max) {
        requireNonEmpty(value, fieldName);
        if (value.trim().length() < min || value.trim().length() > max)
            throw new IllegalArgumentException(
                    fieldName + " must be " + min + "-" + max + " characters");
    }

    // ─── Format Checks ────────────────────────────────────────────────────

    public static void requireValidEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) return; // email optional
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid " + fieldName + " format");
    }

    public static void requireValidContact(String contact, String fieldName) {
        requireNonEmpty(contact, fieldName);
        if (!contact.matches("^[0-9+\\-]{7,15}$"))
            throw new IllegalArgumentException(
                    fieldName + " must be 7-15 digits (numbers, +, - only)");
    }

    public static void requireValidUsername(String username) {
        requireLength(username, "Username", 3, 50);
        if (!username.matches("^[a-zA-Z0-9_]+$"))
            throw new IllegalArgumentException(
                    "Username can only contain letters, numbers and underscore");
    }

    public static void requireValidRole(String role) {
        if (!"admin".equals(role) && !"receptionist".equals(role))
            throw new IllegalArgumentException(
                    "Role must be 'admin' or 'receptionist'");
    }

    public static void requireValidRoomType(String type) {
        if (!java.util.List.of("Single","Double","Deluxe","Suite").contains(type))
            throw new IllegalArgumentException(
                    "Room type must be Single, Double, Deluxe or Suite");
    }

    public static void requireValidRoomStatus(String status) {
        if (!java.util.List.of("AVAILABLE","OCCUPIED","MAINTENANCE").contains(status))
            throw new IllegalArgumentException(
                    "Room status must be AVAILABLE, OCCUPIED or MAINTENANCE");
    }

    // ─── Number Checks ────────────────────────────────────────────────────

    public static void requirePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0)
            throw new IllegalArgumentException(
                    fieldName + " must be greater than 0");
    }

    // ─── Date Checks ──────────────────────────────────────────────────────

    public static java.time.LocalDate parseDate(String value, String fieldName) {
        try {
            return java.time.LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid date (YYYY-MM-DD)");
        }
    }

    public static void requireFutureOrToday(java.time.LocalDate date,
                                            String fieldName) {
        if (date.isBefore(java.time.LocalDate.now()))
            throw new IllegalArgumentException(
                    fieldName + " cannot be in the past");
    }

    public static void requireAfter(java.time.LocalDate later,
                                    java.time.LocalDate earlier,
                                    String laterName,
                                    String earlierName) {
        if (!later.isAfter(earlier))
            throw new IllegalArgumentException(
                    laterName + " must be after " + earlierName);
    }
}