package com.roomreservation.model;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for Reservation model class.
 *
 * Requirements covered:
 *  - UC-02: Add New Reservation
 *  - UC-06: Manage Reservation (Update / Cancel)
 *  - Class Diagram: ReservationStatus enumeration
 */
@DisplayName("Reservation Model Tests")
public class ReservationTest {

    private Guest  guest;
    private Room   room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        guest    = new Guest("G001", "Dilnoza Karimova", "45 Lake Rd, Galle", "0712345678");
        room     = new Room("R201", "201", RoomType.DOUBLE, RoomStatus.AVAILABLE, 2);
        checkIn  = LocalDate.of(2026, 3, 10);
        checkOut = LocalDate.of(2026, 3, 14);
        reservation = new Reservation("RES-001", guest, room, checkIn, checkOut);
    }

    // ── TC-RES-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-01: Reservation created with all fields set correctly")
    void testReservationCreation() {
        assertNotNull(reservation);
        assertEquals("RES-001",   reservation.getReservationNumber());
        assertEquals(guest,       reservation.getGuest());
        assertEquals(room,        reservation.getRoom());
        assertEquals(checkIn,     reservation.getCheckInDate());
        assertEquals(checkOut,    reservation.getCheckOutDate());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    // ── TC-RES-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-02: Night count calculation is correct")
    void testNightCountCalculation() {
        // 10 Mar → 14 Mar = 4 nights
        assertEquals(4, reservation.calculateNights(),
                "Should be 4 nights for Mar 10-14");
    }

    // ── TC-RES-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-03: Single-night stay returns 1")
    void testSingleNightStay() {
        Reservation singleNight = new Reservation(
                "RES-002", guest, room,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 2));
        assertEquals(1, singleNight.calculateNights());
    }

    // ── TC-RES-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-04: Check-out before check-in should throw")
    void testCheckOutBeforeCheckIn() {
        assertThrows(IllegalArgumentException.class, () ->
                        new Reservation("RES-ERR", guest, room,
                                LocalDate.of(2026, 3, 14),
                                LocalDate.of(2026, 3, 10)),
                "Check-out before check-in must throw IllegalArgumentException");
    }

    // ── TC-RES-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-05: Same check-in and check-out date should throw")
    void testSameDateCheckInOut() {
        LocalDate same = LocalDate.of(2026, 3, 10);
        assertThrows(IllegalArgumentException.class, () ->
                        new Reservation("RES-ERR", guest, room, same, same),
                "Zero-night reservation must throw");
    }

    // ── TC-RES-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-06: Status changes to CANCELLED correctly")
    void testCancelReservation() {
        reservation.cancel();
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    // ── TC-RES-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-07: Status changes to CHECKED_OUT correctly")
    void testCheckOutReservation() {
        reservation.checkOut();
        assertEquals(ReservationStatus.CHECKED_OUT, reservation.getStatus());
    }

    // ── TC-RES-08 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-08: Cannot cancel a CHECKED_OUT reservation")
    void testCannotCancelCheckedOut() {
        reservation.checkOut();
        assertThrows(IllegalStateException.class,
                () -> reservation.cancel(),
                "Cannot cancel a reservation that is already checked out");
    }

    // ── TC-RES-09 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-09: Reservation number is auto-generated when null passed")
    void testAutoGenerateReservationNumber() {
        Reservation auto = new Reservation(null, guest, room, checkIn, checkOut);
        assertNotNull(auto.getReservationNumber(),
                "Reservation number should be auto-generated when null is passed");
        assertFalse(auto.getReservationNumber().isBlank());
    }

    // ── TC-RES-10 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RES-10: Guest and Room cannot be null")
    void testNullGuestOrRoomThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("RES-X", null, room, checkIn, checkOut));
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation("RES-X", guest, null, checkIn, checkOut));
    }
}