package com.roomreservation.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for Room model class.
 *
 * Requirements covered:
 *  - UC-05: Check Room Availability
 *  - Class Diagram: RoomStatus enumeration (AVAILABLE / OCCUPIED)
 *  - Class Diagram: RoomType enumeration (SINGLE / DOUBLE / DELUXE / SUITE)
 */
@DisplayName("Room Model Tests")
public class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("R101", "101", RoomType.SINGLE, RoomStatus.AVAILABLE, 1);
    }

    // ── TC-RM-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-01: Room created with valid data")
    void testRoomCreation() {
        assertNotNull(room);
        assertEquals("R101",            room.getRoomId());
        assertEquals("101",             room.getRoomNumber());
        assertEquals(RoomType.SINGLE,   room.getRoomType());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
        assertEquals(1,                 room.getFloorNumber());
    }

    // ── TC-RM-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-02: New room defaults to AVAILABLE status")
    void testDefaultStatus() {
        assertEquals(RoomStatus.AVAILABLE, room.getStatus(),
                "Newly created room must have AVAILABLE status");
    }

    // ── TC-RM-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-03: Room status changes to OCCUPIED after booking")
    void testStatusChangeToOccupied() {
        room.setStatus(RoomStatus.OCCUPIED);
        assertEquals(RoomStatus.OCCUPIED, room.getStatus());
    }

    // ── TC-RM-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-04: Room status reverts to AVAILABLE on checkout")
    void testStatusRevertToAvailable() {
        room.setStatus(RoomStatus.OCCUPIED);
        room.setStatus(RoomStatus.AVAILABLE);
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
    }

    // ── TC-RM-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-05: All RoomType enum values exist")
    void testRoomTypeEnumValues() {
        assertDoesNotThrow(() -> RoomType.valueOf("SINGLE"));
        assertDoesNotThrow(() -> RoomType.valueOf("DOUBLE"));
        assertDoesNotThrow(() -> RoomType.valueOf("DELUXE"));
        assertDoesNotThrow(() -> RoomType.valueOf("SUITE"));
    }

    // ── TC-RM-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-06: Room ID cannot be null")
    void testRoomIdNotNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(null, "102", RoomType.DOUBLE, RoomStatus.AVAILABLE, 1));
    }

    // ── TC-RM-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-07: Floor number cannot be negative")
    void testFloorNumberNonNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room("R001", "001", RoomType.SINGLE, RoomStatus.AVAILABLE, -1),
                "Negative floor number should throw");
    }

    // ── TC-RM-08 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-RM-08: isAvailable() helper returns correct boolean")
    void testIsAvailableHelper() {
        assertTrue(room.isAvailable(),  "Room should report available");
        room.setStatus(RoomStatus.OCCUPIED);
        assertFalse(room.isAvailable(), "Occupied room should not report available");
    }
}