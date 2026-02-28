package com.roomreservation.store;

import com.roomreservation.model.Reservation;
import com.roomreservation.model.ReservationStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReservationStore {

    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(1000);

    public void saveReservation(Reservation reservation) {
        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");
        if (reservations.containsKey(reservation.getReservationNumber()))
            throw new IllegalStateException(
                    "Duplicate reservation number: " + reservation.getReservationNumber());
        reservations.put(reservation.getReservationNumber(), reservation);
    }

    public Reservation fetchByReservationNo(String number) {
        return reservations.get(number);
    }

    public String generateReservationNumber() {
        return "RES-" + String.format("%05d", counter.getAndIncrement());
    }

    public void updateReservationStatus(String number, ReservationStatus status) {
        Reservation r = reservations.get(number);
        if (r == null)
            throw new IllegalArgumentException("Reservation not found: " + number);
        if (status == ReservationStatus.CANCELLED) r.cancel();
        else if (status == ReservationStatus.CHECKED_OUT) r.checkOut();
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }
}
