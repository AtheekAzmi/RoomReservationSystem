package com.roomreservation.service;

import com.roomreservation.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {

    private static final int MAX_ATTEMPTS = 3;

    private final DataStore dataStore;
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    public AuthenticationService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public boolean authenticate(String username, String password) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        if (password == null) throw new IllegalArgumentException("Password cannot be null");

        if (isAccountLocked(username)) return false;

        User user = dataStore.fetchUser(username);
        if (user == null) return false;

        if (hashPassword(password).equals(user.getHashedPassword())) {
            failedAttempts.remove(username);
            return true;
        }

        failedAttempts.merge(username, 1, Integer::sum);
        return false;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAccountLocked(String username) {
        return failedAttempts.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }
}
