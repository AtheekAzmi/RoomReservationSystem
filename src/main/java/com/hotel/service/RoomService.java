package com.hotel.service;

import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.dao.RoomRateDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomRate;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDAO        roomDAO  = new RoomDAO();
    private final RoomRateDAO    rateDAO  = new RoomRateDAO();
    private final ReservationDAO resDAO   = new ReservationDAO();

    public String getAllRooms() {
        List<Room> list = roomDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Room r : list) arr.put(toJson(r));
        return arr.toString();
    }

    public String getAvailableRooms(String typeName,
                                    String checkinStr,
                                    String checkoutStr) {
        // Validate inputs
        if (typeName == null || typeName.isEmpty())
            throw new IllegalArgumentException("Room type is required");
        if (!List.of("Single","Double","Deluxe","Suite").contains(typeName))
            throw new IllegalArgumentException("Invalid room type");

        LocalDate checkin  = parseDate(checkinStr,  "Check-in date");
        LocalDate checkout = parseDate(checkoutStr, "Check-out date");

        if (!checkin.isBefore(checkout))
            throw new IllegalArgumentException("Check-out must be after check-in");

        List<Room> list = roomDAO.findAvailableRooms(typeName, checkin, checkout);
        JSONArray arr = new JSONArray();
        for (Room r : list) arr.put(toJson(r));
        return arr.toString();
    }

    public String createRoom(JSONObject json) {
        String roomNumber = json.optString("roomNumber", "").trim();
        int    floor      = json.optInt   ("floorNumber", 0);
        String typeName   = json.optString("typeName",   "").trim();
        String status     = json.optString("roomStatus", "AVAILABLE").trim();

        if (roomNumber.isEmpty())
            throw new IllegalArgumentException("Room number is required");
        if (floor < 1 || floor > 50)
            throw new IllegalArgumentException("Floor number must be between 1 and 50");
        if (!List.of("Single","Double","Deluxe","Suite").contains(typeName))
            throw new IllegalArgumentException("Room type must be Single, Double, Deluxe or Suite");
        if (!List.of("AVAILABLE","OCCUPIED","MAINTENANCE").contains(status))
            throw new IllegalArgumentException("Invalid room status");
        if (roomDAO.roomNumberExists(roomNumber, 0))
            throw new IllegalArgumentException("Room number '" + roomNumber + "' already exists");

        int typeId = roomDAO.findTypeIdByName(typeName);
        if (typeId == -1)
            throw new IllegalArgumentException("Room type not found in database");

        Room room = new Room(roomNumber, floor, typeId, status);
        boolean ok = roomDAO.save(room);
        if (!ok) throw new RuntimeException("Failed to create room");
        return new JSONObject().put("message", "Room created successfully").toString();
    }

    public String updateRoom(int roomId, JSONObject json) {
        Room room = roomDAO.findById(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found");

        String roomNumber = json.optString("roomNumber",  room.getRoomNumber()).trim();
        int    floor      = json.optInt   ("floorNumber", room.getFloorNumber());
        String typeName   = json.optString("typeName",    room.getTypeName()).trim();
        String status     = json.optString("roomStatus",  room.getRoomStatus()).trim();

        if (roomNumber.isEmpty())
            throw new IllegalArgumentException("Room number is required");
        if (floor < 1 || floor > 50)
            throw new IllegalArgumentException("Floor number must be between 1 and 50");
        if (!List.of("Single","Double","Deluxe","Suite").contains(typeName))
            throw new IllegalArgumentException("Invalid room type");
        if (!List.of("AVAILABLE","OCCUPIED","MAINTENANCE").contains(status))
            throw new IllegalArgumentException("Invalid room status");
        if (roomDAO.roomNumberExists(roomNumber, roomId))
            throw new IllegalArgumentException("Room number '" + roomNumber + "' already in use");

        int typeId = roomDAO.findTypeIdByName(typeName);
        if (typeId == -1)
            throw new IllegalArgumentException("Room type not found in database");

        room.setRoomNumber (roomNumber);
        room.setFloorNumber(floor);
        room.setRoomTypeId (typeId);
        room.setRoomStatus (status);

        boolean ok = roomDAO.update(room);
        if (!ok) throw new RuntimeException("Failed to update room");
        return new JSONObject().put("message", "Room updated successfully").toString();
    }

    public String deleteRoom(int roomId) {
        Room room = roomDAO.findById(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found");
        if (resDAO.countActiveByRoomId(roomId) > 0)
            throw new IllegalArgumentException(
                "Cannot delete room with active reservations. Cancel or check out those reservations first.");
        boolean ok = roomDAO.delete(roomId);
        if (!ok) throw new RuntimeException("Failed to delete room");
        return new JSONObject().put("message", "Room deleted successfully").toString();
    }

    public String updateRoomStatus(int roomId, String status) {
        List<String> valid = List.of("AVAILABLE","OCCUPIED","MAINTENANCE");
        if (!valid.contains(status))
            throw new IllegalArgumentException("Invalid room status");
        boolean ok = roomDAO.updateStatus(roomId, status);
        if (!ok) throw new RuntimeException("Update failed");
        return new JSONObject().put("message", "Room status updated").toString();
    }

    public Room getRoomById(int roomId) {
        Room r = roomDAO.findById(roomId);
        if (r == null) throw new IllegalArgumentException("Room not found: " + roomId);
        return r;
    }

    public RoomRate getCurrentRate(int roomId) {
        Room r = roomDAO.findById(roomId);
        if (r == null) throw new IllegalArgumentException("Room not found");
        RoomRate rate = rateDAO.findCurrentRate(r.getRoomTypeId());
        if (rate == null) throw new RuntimeException("No rate found for this room type");
        return rate;
    }

    private LocalDate parseDate(String s, String fieldName) {
        try { return LocalDate.parse(s); }
        catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " is invalid (use YYYY-MM-DD)");
        }
    }

    private JSONObject toJson(Room r) {
        return new JSONObject()
                .put("roomId",      r.getRoomId())
                .put("roomNumber",  r.getRoomNumber())
                .put("floorNumber", r.getFloorNumber())
                .put("roomTypeId",  r.getRoomTypeId())
                .put("typeName",    r.getTypeName() != null ? r.getTypeName() : "")
                .put("roomStatus",  r.getRoomStatus());
    }
}