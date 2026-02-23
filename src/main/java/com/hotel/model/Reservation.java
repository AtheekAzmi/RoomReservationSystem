package com.hotel.model;

import java.time.LocalDate;

public class Reservation {
    private int       reservationId;
    private String    reservationNumber;
    private int       guestId;
    private int       roomId;
    private int       staffId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private String    status; // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    // Joined display fields
    private String guestName;
    private String roomNumber;
    private String staffName;

    public Reservation() {}

    public Reservation(String reservationNumber, int guestId, int roomId,
                       int staffId, LocalDate checkinDate,
                       LocalDate checkoutDate) {
        this.reservationNumber = reservationNumber;
        this.guestId           = guestId;
        this.roomId            = roomId;
        this.staffId           = staffId;
        this.checkinDate       = checkinDate;
        this.checkoutDate      = checkoutDate;
        this.status            = "CONFIRMED";
    }

    public int       getReservationId()           { return reservationId; }
    public void      setReservationId(int id)     { this.reservationId = id; }

    public String    getReservationNumber()            { return reservationNumber; }
    public void      setReservationNumber(String n)    { this.reservationNumber = n; }

    public int       getGuestId()          { return guestId; }
    public void      setGuestId(int id)    { this.guestId = id; }

    public int       getRoomId()           { return roomId; }
    public void      setRoomId(int id)     { this.roomId = id; }

    public int       getStaffId()          { return staffId; }
    public void      setStaffId(int id)    { this.staffId = id; }

    public LocalDate getCheckinDate()              { return checkinDate; }
    public void      setCheckinDate(LocalDate d)   { this.checkinDate = d; }

    public LocalDate getCheckoutDate()             { return checkoutDate; }
    public void      setCheckoutDate(LocalDate d)  { this.checkoutDate = d; }

    public String    getStatus()           { return status; }
    public void      setStatus(String s)   { this.status = s; }

    public String    getGuestName()          { return guestName; }
    public void      setGuestName(String n)  { this.guestName = n; }

    public String    getRoomNumber()          { return roomNumber; }
    public void      setRoomNumber(String n)  { this.roomNumber = n; }

    public String    getStaffName()          { return staffName; }
    public void      setStaffName(String n)  { this.staffName = n; }
}