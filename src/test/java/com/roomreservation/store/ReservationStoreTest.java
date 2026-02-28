package com.roomreservation.store;

import com.roomreservation.model.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for ReservationStore.
 *
 * Tests in-memory CRUD operations matching the sequence diagrams:
 *  - saveReservation(reservationDetails)
 *  - fetchByReservationNo(reservationNo)
 *  - generateReservationNumber()
 *
 * Requirements covered:
 *  - UC-02: Add New Reservation
 *  - UC-04: Calculate and Print Bill
 *  - UC-06: Manage Reservation
 */
@DisplayName("ReservationStore Tests")
public class ReservationStoreTest {

    private ReservationStore store;
    private Guest  guest;
    private Room   room;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        store  = new ReservationStore();
        guest  = new Guest("G001", "Saman Silva", "20 River Rd", "0754321098");
        room   = new Room("R101", "101", RoomType.DOUBLE, RoomStatus.AVAILABLE, 1);
        reservation = new Reservation("RES-001", guest, room,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5));
    }

    // ── TC-RS-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-01: Save and fetch reservation by number")
    void testSaveAndFetchByNumber() {
        store.saveReservation(reservation);
        Reservation fetched = store.fetchByReservationNo("RES-001");

        assertNotNull(fetched, "Saved reservation must be retrievable");
        assertEquals("RES-001", fetched.getReservationNumber());
    }

    // ── TC-RS-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-02: Fetch non-existent reservation returns null")
    void testFetchNonExistentReturnsNull() {
        Reservation result = store.fetchByReservationNo("RES-999");
        assertNull(result, "Non-existent reservation number should return null");
    }

    // ── TC-RS-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-03: Generated reservation numbers are unique")
    void testGeneratedNumbersAreUnique() {
        String num1 = store.generateReservationNumber();
        String num2 = store.generateReservationNumber();
        String num3 = store.generateReservationNumber();

        assertNotEquals(num1, num2, "Each generated number must be unique");
        assertNotEquals(num2, num3);
        assertNotEquals(num1, num3);
    }

    // ── TC-RS-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-04: Generated reservation number follows RES-XXXXX format")
    void testGeneratedNumberFormat() {
        String num = store.generateReservationNumber();
        assertTrue(num.startsWith("RES-"),
                "Reservation number must start with RES-");
    }

    // ── TC-RS-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-05: Update reservation status persists correctly")
    void testUpdateReservationStatus() {
        store.saveReservation(reservation);
        store.updateReservationStatus("RES-001", ReservationStatus.CHECKED_OUT);

        Reservation updated = store.fetchByReservationNo("RES-001");
        assertEquals(ReservationStatus.CHECKED_OUT, updated.getStatus());
    }

    // ── TC-RS-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-06: getAllReservations returns all saved entries")
    void testGetAllReservations() {
        store.saveReservation(reservation);
        Room r2 = new Room("R102", "102", RoomType.SUITE, RoomStatus.AVAILABLE, 1);
        Reservation r2Obj = new Reservation("RES-002", guest, r2,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        store.saveReservation(r2Obj);

        List<Reservation> all = store.getAllReservations();
        assertEquals(2, all.size(), "Store should hold 2 reservations");
    }

    // ── TC-RS-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-07: Saving null reservation throws IllegalArgumentException")
    void testSaveNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> store.saveReservation(null));
    }

    // ── TC-RS-08 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RS-08: Duplicate reservation number is rejected")
    void testDuplicateReservationNumberRejected() {
        store.saveReservation(reservation);
        Reservation duplicate = new Reservation("RES-001", guest, room,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5));
        assertThrows(IllegalStateException.class,
                () -> store.saveReservation(duplicate),
                "Duplicate reservation number must throw");
    }
}