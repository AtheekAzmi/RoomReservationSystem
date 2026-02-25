package com.hotel.util;

import com.hotel.model.Staff;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static SessionManager instance;
    private final ConcurrentHashMap<String, Staff> sessions
            = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public String createSession(Staff staff) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, staff);
        System.out.println("[Session] Created for: " + staff.getUsername());
        return token;
    }

    public Staff getSession(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        return sessions.get(token.trim());
    }

    public boolean isValid(String token) {
        boolean v = getSession(token) != null;
        System.out.println("[Session] isValid: " + v);
        return v;
    }

    public boolean isAdmin(String token) {
        Staff s = getSession(token);
        return s != null && "admin".equals(s.getRole());
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }
}