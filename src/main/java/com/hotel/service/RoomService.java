package com.hotel.service;

import com.hotel.dao.RoomDAO;
import com.hotel.dao.RoomRateDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomRate;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDAO     roomDAO     = new RoomDAO();
    private final RoomRateDAO rateDAO     = new RoomRateDAO();

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