package com.roomreservation.service;

import com.roomreservation.model.*;
import com.roomreservation.store.GuestStore;
import com.roomreservation.store.ReservationStore;
import com.roomreservation.store.RoomStore;

import java.time.LocalDate;
import java.util.List;

public class ReservationSystem {

    private final GuestStore       guestStore;
    private final RoomStore        roomStore;
    private final ReservationStore reservationStore;

    public ReservationSystem(GuestStore guestStore, RoomStore roomStore,
                             ReservationStore reservationStore) {
        this.guestStore       = guestStore;
        this.roomStore        = roomStore;
        this.reservationStore = reservationStore;
    }

    public Guest findOrCreateGuest(String name, String address, String contactNumber) {
        Guest existing = guestStore.findByContact(contactNumber);
        return (existing != null) ? existing : guestStore.createGuest(name, address, contactNumber);
    }

    public List<Room> checkAvailability(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        return roomStore.getAvailableRooms(type, checkIn, checkOut);
    }

    public Reservation addReservation(Guest guest, Room room,
                                      LocalDate checkIn, LocalDate checkOut) {
        String resNo = reservationStore.generateReservationNumber();
        Reservation reservation = new Reservation(resNo, guest, room, checkIn, checkOut);
        roomStore.updateRoomStatus(room.getRoomNumber(), RoomStatus.OCCUPIED);
        reservationStore.saveReservation(reservation);
        return reservation;
    }

    public Reservation findReservation(String reservationNumber) {
        return reservationStore.fetchByReservationNo(reservationNumber);
    }

    public Bill generateBill(String reservationNumber, double ratePerNight) {
        Reservation reservation = reservationStore.fetchByReservationNo(reservationNumber);
        if (reservation == null)
            throw new IllegalArgumentException("Reservation not found: " + reservationNumber);
        if (reservation.getStatus() == ReservationStatus.CANCELLED)
            throw new IllegalStateException("Cannot generate bill for a cancelled reservation");
        return new Bill(reservation, ratePerNight);
    }

    public void checkout(String reservationNumber) {
        reservationStore.updateReservationStatus(reservationNumber, ReservationStatus.CHECKED_OUT);
    }

    public void cancelReservation(String reservationNumber) {
        reservationStore.updateReservationStatus(reservationNumber, ReservationStatus.CANCELLED);
    }
}
