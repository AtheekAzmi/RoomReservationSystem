package com.roomreservation.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for Guest model class.
 * Tests written BEFORE implementation to drive design.
 *
 * Requirements covered:
 *  - UC-02: Add New Reservation (guest creation sub-flow)
 *  - UC-05: Check Room Availability
 */
@DisplayName("Guest Model Tests")
public class Guesttest {

    private Guest guest;

    @BeforeEach
    void setUp() {
        guest = new Guest("G001", "Aisha Fernando", "123 Main St, Colombo", "0771234567");
    }

    // ── TC-GM-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-01: Guest created with valid data")
    void testGuestCreatedSuccessfully() {
        assertNotNull(guest, "Guest object should not be null");
        assertEquals("G001",           guest.getGuestId());
        assertEquals("Aisha Fernando", guest.getGuestName());
        assertEquals("123 Main St, Colombo", guest.getAddress());
        assertEquals("0771234567",     guest.getContactNumber());
    }

    // ── TC-GM-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-02: Guest ID cannot be null or empty")
    void testGuestIdNotNullOrEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest(null, "Aisha Fernando", "123 Main St", "0771234567"),
                "Null guest ID should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class,
                () -> new Guest("", "Aisha Fernando", "123 Main St", "0771234567"),
                "Empty guest ID should throw IllegalArgumentException");
    }

    // ── TC-GM-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-03: Guest name cannot be null or empty")
    void testGuestNameValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("G002", null, "123 Main St", "0771234567"),
                "Null name should throw");

        assertThrows(IllegalArgumentException.class,
                () -> new Guest("G002", "  ", "123 Main St", "0771234567"),
                "Blank name should throw");
    }

    // ── TC-GM-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-04: Contact number format validation (10 digits)")
    void testContactNumberValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("G003", "Test User", "Addr", "123"),
                "Short contact number should throw");

        assertThrows(IllegalArgumentException.class,
                () -> new Guest("G003", "Test User", "Addr", "ABCDEFGHIJ"),
                "Non-numeric contact number should throw");
    }

    // ── TC-GM-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-05: Guest toString contains key identifying info")
    void testToString() {
        String str = guest.toString();
        assertTrue(str.contains("G001"),          "toString should include guestId");
        assertTrue(str.contains("Aisha Fernando"),"toString should include name");
        assertTrue(str.contains("0771234567"),    "toString should include contact");
    }

    // ── TC-GM-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-06: Guest equality based on guestId")
    void testGuestEquality() {
        Guest duplicate = new Guest("G001", "Different Name", "Different Addr", "0779999999");
        assertEquals(guest, duplicate,
                "Guests with the same ID should be considered equal");
    }

    // ── TC-GM-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-GM-07: Two distinct guests with different IDs are not equal")
    void testGuestInequality() {
        Guest other = new Guest("G999", "Other Guest", "Other Addr", "0770000000");
        assertNotEquals(guest, other,
                "Guests with different IDs should not be equal");
    }
}