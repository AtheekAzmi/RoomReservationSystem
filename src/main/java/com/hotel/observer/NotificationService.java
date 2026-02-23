package com.hotel.observer;

import java.util.ArrayList;
import java.util.List;

// Acts as the SUBJECT in the Observer pattern
// Holds all observers and notifies them on events
public class NotificationService {

    // ─── Singleton ────────────────────────────────────────────────────────
    private static NotificationService instance;

    private NotificationService() {
        // Register built-in observers on startup
        addObserver(new AuditLogObserver());
        addObserver(new RoomStatusObserver());
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null)
            instance = new NotificationService();
        return instance;
    }

    // ─── Observer Registry ────────────────────────────────────────────────
    private final List<ReservationObserver> observers = new ArrayList<>();

    public void addObserver(ReservationObserver observer) {
        if (!observers.contains(observer))
            observers.add(observer);
    }

    public void removeObserver(ReservationObserver observer) {
        observers.remove(observer);
    }

    // ─── Notification Methods ─────────────────────────────────────────────

    public void notifyCreated(ReservationEvent event) {
        System.out.println("[NotificationService] Broadcasting CREATED event: "
                + event.getReservation().getReservationNumber());
        for (ReservationObserver o : observers) {
            try {
                o.onReservationCreated(event);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error: "
                        + e.getMessage());
            }
        }
    }

    public void notifyCancelled(ReservationEvent event) {
        System.out.println("[NotificationService] Broadcasting CANCELLED event: "
                + event.getReservation().getReservationNumber());
        for (ReservationObserver o : observers) {
            try {
                o.onReservationCancelled(event);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error: "
                        + e.getMessage());
            }
        }
    }

    public void notifyUpdated(ReservationEvent event) {
        System.out.println("[NotificationService] Broadcasting UPDATED event: "
                + event.getReservation().getReservationNumber());
        for (ReservationObserver o : observers) {
            try {
                o.onReservationUpdated(event);
            } catch (Exception e) {
                System.err.println("[NotificationService] Observer error: "
                        + e.getMessage());
            }
        }
    }
}