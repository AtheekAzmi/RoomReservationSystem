package com.roomreservation.store;

import com.roomreservation.model.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for RoomStore.
 *
 * Covers the availability check logic from the sequence diagram:
 *   getAvailableRooms(roomType, checkIn, checkOut)
 *   updateRoomStatus(roomNo, OCCUPIED)
 *
 * Requirements covered:
 *  - UC-02: Add New Reservation (room availability check)
 *  - UC-05: Check Room Availability
 */
@DisplayName("RoomStore Tests")
public class RoomStoreTest {

    private RoomStore store;
    private Room singleRoom;
    private Room doubleRoom;
    private Room suiteRoom;

    @BeforeEach
    void setUp() {
        store      = new RoomStore();
        singleRoom = new Room("R101", "101", RoomType.SINGLE, RoomStatus.AVAILABLE, 1);
        doubleRoom = new Room("R201", "201", RoomType.DOUBLE, RoomStatus.AVAILABLE, 2);
        suiteRoom  = new Room("R301", "301", RoomType.SUITE,  RoomStatus.AVAILABLE, 3);
        store.addRoom(singleRoom);
        store.addRoom(doubleRoom);
        store.addRoom(suiteRoom);
    }

    // ── TC-ROOM-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-01: Available SINGLE rooms returned for open dates")
    void testGetAvailableSingleRooms() {
        List<Room> available = store.getAvailableRooms(
                RoomType.SINGLE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5));

        assertFalse(available.isEmpty(), "Should return at least one SINGLE room");
        available.forEach(r ->
                assertEquals(RoomType.SINGLE, r.getRoomType()));
    }

    // ── TC-ROOM-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-02: Occupied room not included in availability results")
    void testOccupiedRoomExcluded() {
        store.updateRoomStatus("101", RoomStatus.OCCUPIED);

        List<Room> available = store.getAvailableRooms(
                RoomType.SINGLE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5));

        boolean roomPresent = available.stream()
                .anyMatch(r -> r.getRoomNumber().equals("101"));
        assertFalse(roomPresent, "Occupied room should not appear in availability");
    }

    // ── TC-ROOM-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-03: Empty list returned when all rooms of type are occupied")
    void testEmptyListWhenAllOccupied() {
        store.updateRoomStatus("101", RoomStatus.OCCUPIED);

        List<Room> available = store.getAvailableRooms(
                RoomType.SINGLE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5));

        assertTrue(available.isEmpty(),
                "No available SINGLE rooms should return empty list");
    }

    // ── TC-ROOM-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-04: updateRoomStatus changes status correctly")
    void testUpdateRoomStatus() {
        store.updateRoomStatus("201", RoomStatus.OCCUPIED);
        Room updated = store.getRoomByNumber("201");
        assertEquals(RoomStatus.OCCUPIED, updated.getStatus());
    }

    // ── TC-ROOM-05 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-05: updateRoomStatus on unknown room throws")
    void testUpdateUnknownRoomThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> store.updateRoomStatus("999", RoomStatus.OCCUPIED),
                "Unknown room number should throw");
    }

    // ── TC-ROOM-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-06: getAllRooms returns all added rooms")
    void testGetAllRooms() {
        List<Room> all = store.getAllRooms();
        assertEquals(3, all.size(), "Store should contain 3 rooms");
    }

    // ── TC-ROOM-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-ROOM-07: Adding duplicate room number throws IllegalStateException")
    void testDuplicateRoomRejected() {
        Room dup = new Room("R999", "101", RoomType.SINGLE, RoomStatus.AVAILABLE, 1);
        assertThrows(IllegalStateException.class,
                () -> store.addRoom(dup),
                "Duplicate room number should throw");
    }
}