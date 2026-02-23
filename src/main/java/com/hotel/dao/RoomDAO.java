package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public boolean save(Room r) {
        String sql = "INSERT INTO room(room_number, floor_number, room_type_id, room_status) " +
                "VALUES(?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setInt   (2, r.getFloorNumber());
            ps.setInt   (3, r.getRoomTypeId());
            ps.setString(4, r.getRoomStatus() != null ? r.getRoomStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Room findById(int roomId) {
        String sql = "SELECT r.*, rt.type_name FROM room r " +
                "JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                "WHERE r.room_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Room findByNumber(String roomNumber) {
        String sql = "SELECT r.*, rt.type_name FROM room r " +
                "JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                "WHERE r.room_number=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rt.type_name FROM room r " +
                "JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                "ORDER BY r.floor_number, r.room_number";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Room> findAvailableRooms(String typeName,
                                         LocalDate checkin,
                                         LocalDate checkout) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rt.type_name FROM room r " +
                "JOIN room_type rt ON r.room_type_id = rt.room_type_id " +
                "WHERE rt.type_name = ? " +
                "AND r.room_status = 'AVAILABLE' " +
                "AND r.room_id NOT IN (" +
                "  SELECT room_id FROM reservation " +
                "  WHERE status NOT IN ('CANCELLED','CHECKED_OUT') " +
                "  AND NOT (checkout_date <= ? OR checkin_date >= ?)" +
                ")";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, typeName);
            ps.setDate  (2, Date.valueOf(checkin));
            ps.setDate  (3, Date.valueOf(checkout));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(int roomId, String status) {
        String sql = "UPDATE room SET room_status=? WHERE room_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Room r) {
        String sql = "UPDATE room SET room_number=?, floor_number=?, " +
                "room_type_id=?, room_status=? WHERE room_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setInt   (2, r.getFloorNumber());
            ps.setInt   (3, r.getRoomTypeId());
            ps.setString(4, r.getRoomStatus());
            ps.setInt   (5, r.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId    (rs.getInt   ("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setFloorNumber(rs.getInt  ("floor_number"));
        r.setRoomTypeId(rs.getInt   ("room_type_id"));
        r.setRoomStatus(rs.getString("room_status"));
        r.setTypeName  (rs.getString("type_name"));
        return r;
    }
}