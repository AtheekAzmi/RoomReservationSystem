package com.roomreservation.store;

import com.roomreservation.model.Room;
import com.roomreservation.model.RoomStatus;
import com.roomreservation.model.RoomType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoomStore {

    private final Map<String, Room> rooms = new LinkedHashMap<>();

    public void addRoom(Room room) {
        if (rooms.containsKey(room.getRoomNumber()))
            throw new IllegalStateException("Duplicate room number: " + room.getRoomNumber());
        rooms.put(room.getRoomNumber(), room);
    }

    /** Returns all rooms of the given type that are currently AVAILABLE. */
    public List<Room> getAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        List<Room> result = new ArrayList<>();
        for (Room r : rooms.values()) {
            if (r.getRoomType() == type && r.isAvailable())
                result.add(r);
        }
        return result;
    }

    public void updateRoomStatus(String roomNumber, RoomStatus status) {
        Room room = rooms.get(roomNumber);
        if (room == null)
            throw new IllegalArgumentException("Room not found: " + roomNumber);
        room.setStatus(status);
    }

    public Room getRoomByNumber(String roomNumber) {
        return rooms.get(roomNumber);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
}
