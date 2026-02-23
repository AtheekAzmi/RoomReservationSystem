package com.hotel.util;

import com.hotel.model.Staff;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // ─── Singleton ────────────────────────────────────────────────────────
    private static SessionManager instance;

    private SessionManager() {
        // Start cleanup thread to remove expired sessions
        startCleanupThread();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    // ─── Session Storage ──────────────────────────────────────────────────
    // Holds token → SessionData
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    // Session expires after 8 hours
    private static final int SESSION_HOURS = 8;

    // ─── Inner class to hold session info ─────────────────────────────────
    private static class SessionData {
        final Staff         staff;
        final LocalDateTime createdAt;
        LocalDateTime       lastAccessed;

        SessionData(Staff staff) {
            this.staff        = staff;
            this.createdAt    = LocalDateTime.now();
            this.lastAccessed = LocalDateTime.now();
        }

        boolean isExpired() {
            return lastAccessed.plusHours(SESSION_HOURS)
                    .isBefore(LocalDateTime.now());
        }
    }

    // ─── Public Methods ───────────────────────────────────────────────────

    // Create a new session and return the token
    public String createSession(Staff staff) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(staff));
        return token;
    }

    // Get the Staff object for a token (null if invalid/expired)
    public Staff getSession(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        if (data == null)      return null;
        if (data.isExpired()) {
            sessions.remove(token);
            return null;
        }
        // Refresh last accessed time
        data.lastAccessed = LocalDateTime.now();
        return data.staff;
    }

    // Check if a token is valid and not expired
    public boolean isValid(String token) {
        return getSession(token) != null;
    }

    // Check if the session belongs to an admin
    public boolean isAdmin(String token) {
        Staff s = getSession(token);
        return s != null && "admin".equals(s.getRole());
    }

    // Invalidate (logout) a session
    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }

    // Get number of active sessions
    public int getActiveSessionCount() {
        return sessions.size();
    }

    // ─── Cleanup Thread ───────────────────────────────────────────────────
    // Runs every 30 minutes to remove expired sessions from memory
    private void startCleanupThread() {
        Thread cleanup = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30 * 60 * 1000); // every 30 minutes
                    sessions.entrySet()
                            .removeIf(e -> e.getValue().isExpired());
                    System.out.println("[SessionManager] Cleanup done. " +
                            "Active sessions: " + sessions.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanup.setDaemon(true); // won't block JVM shutdown
        cleanup.setName("session-cleanup");
        cleanup.start();
    }
}