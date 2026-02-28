package com.roomreservation.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Reservation {

    private final String reservationNumber;
    private final Guest  guest;
    private final Room   room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private ReservationStatus status;

    public Reservation(String reservationNumber, Guest guest, Room room,
                       LocalDate checkIn, LocalDate checkOut) {
        if (guest == null)
            throw new IllegalArgumentException("Guest cannot be null");
        if (room == null)
            throw new IllegalArgumentException("Room cannot be null");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Check-out must be after check-in");

        this.reservationNumber = (reservationNumber != null)
                ? reservationNumber
                : "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.guest       = guest;
        this.room        = room;
        this.checkInDate  = checkIn;
        this.checkOutDate = checkOut;
        this.status       = ReservationStatus.ACTIVE;
    }

    public String            getReservationNumber() { return reservationNumber; }
    public Guest             getGuest()             { return guest; }
    public Room              getRoom()              { return room; }
    public LocalDate         getCheckInDate()       { return checkInDate; }
    public LocalDate         getCheckOutDate()      { return checkOutDate; }
    public ReservationStatus getStatus()            { return status; }

    public int calculateNights() {
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public void cancel() {
        if (status == ReservationStatus.CHECKED_OUT)
            throw new IllegalStateException("Cannot cancel a reservation that is already checked out");
        this.status = ReservationStatus.CANCELLED;
    }

    public void checkOut() {
        this.status = ReservationStatus.CHECKED_OUT;
    }
}
