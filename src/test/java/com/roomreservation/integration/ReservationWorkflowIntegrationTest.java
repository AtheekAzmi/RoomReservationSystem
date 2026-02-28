package com.roomreservation.integration;

import com.roomreservation.model.*;
import com.roomreservation.service.ReservationSystem;
import com.roomreservation.store.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test Suite — Full Reservation Workflow
 *
 * Validates the end-to-end flow documented in the sequence diagrams:
 *   Add New Reservation → Generate Bill → Checkout
 *
 * Requirements covered:
 *  - UC-02: Add New Reservation (complete flow)
 *  - UC-04: Calculate and Print Bill (complete flow)
 *  - UC-05: Check Room Availability
 *  - UC-06: Manage Reservation
 */
@DisplayName("Integration Tests — Reservation Workflow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationWorkflowIntegrationTest {

    private static ReservationSystem system;
    private static String createdReservationNo;

    @BeforeAll
    static void setUpSystem() {
        GuestStore       guestStore       = new GuestStore();
        RoomStore        roomStore        = new RoomStore();
        ReservationStore reservationStore = new ReservationStore();

        // Seed rooms
        roomStore.addRoom(new Room("R101","101", RoomType.SINGLE,  RoomStatus.AVAILABLE, 1));
        roomStore.addRoom(new Room("R201","201", RoomType.DOUBLE,  RoomStatus.AVAILABLE, 2));
        roomStore.addRoom(new Room("R301","301", RoomType.DELUXE,  RoomStatus.AVAILABLE, 3));
        roomStore.addRoom(new Room("R401","401", RoomType.SUITE,   RoomStatus.AVAILABLE, 4));

        system = new ReservationSystem(guestStore, roomStore, reservationStore);
    }

    // ── TC-INT-01 ──────────────────────────────────────────────
    @Test
    @Order(1)
    @DisplayName("TC-INT-01: findOrCreateGuest returns new guest when contact not found")
    void testFindOrCreateNewGuest() {
        Guest guest = system.findOrCreateGuest("Nimal Jayawardena", "45 Hill St", "0710001111");
        assertNotNull(guest);
        assertEquals("Nimal Jayawardena", guest.getGuestName());
        assertEquals("0710001111",        guest.getContactNumber());
    }

    // ── TC-INT-02 ──────────────────────────────────────────────
    @Test
    @Order(2)
    @DisplayName("TC-INT-02: findOrCreateGuest returns existing guest on second call")
    void testFindExistingGuest() {
        system.findOrCreateGuest("Nimal Jayawardena", "45 Hill St", "0710001111");
        Guest found = system.findOrCreateGuest("Nimal Jayawardena", "45 Hill St", "0710001111");
        assertNotNull(found);
        assertEquals("0710001111", found.getContactNumber());
    }

    // ── TC-INT-03 ──────────────────────────────────────────────
    @Test
    @Order(3)
    @DisplayName("TC-INT-03: checkAvailability returns non-empty list for open dates")
    void testRoomAvailabilityReturnsRooms() {
        List<Room> available = system.checkAvailability(
                RoomType.DOUBLE,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5));

        assertFalse(available.isEmpty(), "Should find available DOUBLE rooms");
    }

    // ── TC-INT-04 ──────────────────────────────────────────────
    @Test
    @Order(4)
    @DisplayName("TC-INT-04: addReservation creates reservation and sets room OCCUPIED")
    void testAddReservationSetsRoomOccupied() {
        Guest guest = system.findOrCreateGuest("Kamala Ratnam", "12 Coast Ave", "0729998888");
        List<Room> available = system.checkAvailability(
                RoomType.SINGLE,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 13));
        assertFalse(available.isEmpty());

        Room chosen = available.get(0);
        Reservation res = system.addReservation(guest, chosen,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 13));

        createdReservationNo = res.getReservationNumber();

        assertNotNull(res);
        assertNotNull(createdReservationNo);
        assertEquals(ReservationStatus.ACTIVE, res.getStatus());
        assertEquals(RoomStatus.OCCUPIED, chosen.getStatus());
    }

    // ── TC-INT-05 ──────────────────────────────────────────────
    @Test
    @Order(5)
    @DisplayName("TC-INT-05: findReservation retrieves the saved reservation")
    void testFindReservation() {
        assumeReservationCreated();
        Reservation found = system.findReservation(createdReservationNo);
        assertNotNull(found);
        assertEquals(createdReservationNo, found.getReservationNumber());
    }

    // ── TC-INT-06 ──────────────────────────────────────────────
    @Test
    @Order(6)
    @DisplayName("TC-INT-06: Bill generated with correct total for the reservation")
    void testBillGeneration() {
        assumeReservationCreated();
        Bill bill = system.generateBill(createdReservationNo, 5000.0);

        assertNotNull(bill);
        bill.calculateTotal();
        // 3 nights × 5000 = 15000, + 10% tax = 16500
        assertEquals(15000.0, bill.getSubtotal(),    0.001);
        assertEquals(1500.0,  bill.getTaxAmount(),   0.001);
        assertEquals(16500.0, bill.getTotalAmount(), 0.001);
    }

    // ── TC-INT-07 ──────────────────────────────────────────────
    @Test
    @Order(7)
    @DisplayName("TC-INT-07: Checkout updates reservation status to CHECKED_OUT")
    void testCheckoutUpdatesStatus() {
        assumeReservationCreated();
        system.checkout(createdReservationNo);
        Reservation res = system.findReservation(createdReservationNo);
        assertEquals(ReservationStatus.CHECKED_OUT, res.getStatus());
    }

    // ── TC-INT-08 ──────────────────────────────────────────────
    @Test
    @Order(8)
    @DisplayName("TC-INT-08: Cancelled reservation cannot generate a bill")
    void testCancelledReservationNoBill() {
        Guest guest = system.findOrCreateGuest("John Cancel", "1 Test Rd", "0700000001");
        List<Room> rooms = system.checkAvailability(
                RoomType.SUITE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 4));
        Reservation res = system.addReservation(guest, rooms.get(0),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 4));

        system.cancelReservation(res.getReservationNumber());

        assertThrows(IllegalStateException.class,
                () -> system.generateBill(res.getReservationNumber(), 20000.0),
                "Cannot generate bill for a cancelled reservation");
    }

    // ── TC-INT-09 ──────────────────────────────────────────────
    @Test
    @Order(9)
    @DisplayName("TC-INT-09: findReservation returns null for unknown number")
    void testFindReservationUnknownReturnsNull() {
        Reservation notFound = system.findReservation("RES-UNKNOWN");
        assertNull(notFound, "Should return null for unknown reservation number");
    }

    // helper ────────────────────────────────────────────────────
    private void assumeReservationCreated() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                createdReservationNo != null,
                "Requires TC-INT-04 to have run first");
    }
}