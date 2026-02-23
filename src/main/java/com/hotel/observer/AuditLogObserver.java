package com.hotel.observer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Observer 1: Writes every reservation event to an audit log file
public class AuditLogObserver implements ReservationObserver {

    private static final String LOG_FILE = "reservation_audit.log";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReservationCreated(ReservationEvent event) {
        writeLog("CREATED",
                event.getReservation().getReservationNumber(),
                "Guest ID: " + event.getReservation().getGuestId() +
                        " | Room ID: " + event.getReservation().getRoomId() +
                        " | Check-in: " + event.getReservation().getCheckinDate() +
                        " | Check-out: " + event.getReservation().getCheckoutDate());
    }

    @Override
    public void onReservationCancelled(ReservationEvent event) {
        writeLog("CANCELLED",
                event.getReservation().getReservationNumber(),
                "Reservation cancelled at " + LocalDateTime.now().format(FMT));
    }

    @Override
    public void onReservationUpdated(ReservationEvent event) {
        writeLog("UPDATED",
                event.getReservation().getReservationNumber(),
                "New status: " + event.getReservation().getStatus());
    }

    private void writeLog(String action, String reservationNo, String details) {
        String timestamp = LocalDateTime.now().format(FMT);
        String line = String.format("[%s] ACTION=%-10s | RESERVATION=%-15s | %s",
                timestamp, action, reservationNo, details);
        // Print to console
        System.out.println("[AuditLog] " + line);

        // Also write to file
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(LOG_FILE, true))) { // true = append mode
            pw.println(line);
        } catch (IOException e) {
            System.err.println("[AuditLog] Could not write to log file: "
                    + e.getMessage());
        }
    }
}