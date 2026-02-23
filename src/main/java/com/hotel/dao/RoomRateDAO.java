package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.RoomRate;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomRateDAO {

    public List<RoomRate> findAll() {
        List<RoomRate> list = new ArrayList<>();
        String sql = "SELECT * FROM room_rate ORDER BY room_type_id";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Get the currently active rate for a room type
    public RoomRate findCurrentRate(int roomTypeId) {
        String sql = "SELECT * FROM room_rate " +
                "WHERE room_type_id=? " +
                "AND effective_from <= CURDATE() " +
                "AND (effective_to IS NULL OR effective_to >= CURDATE()) " +
                "ORDER BY effective_from DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Get rate by roomId (joins through room table)
    public RoomRate findRateByRoomId(int roomId) {
        String sql = "SELECT rr.* FROM room_rate rr " +
                "JOIN room r ON rr.room_type_id = r.room_type_id " +
                "WHERE r.room_id=? " +
                "AND rr.effective_from <= CURDATE() " +
                "AND (rr.effective_to IS NULL OR rr.effective_to >= CURDATE()) " +
                "ORDER BY rr.effective_from DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private RoomRate mapRow(ResultSet rs) throws SQLException {
        RoomRate rr = new RoomRate();
        rr.setRoomRateId  (rs.getInt        ("room_rate_id"));
        rr.setRoomTypeId  (rs.getInt        ("room_type_id"));
        rr.setRatePerNight(rs.getBigDecimal ("rate_per_night"));
        rr.setMaxOccupancy(rs.getInt        ("max_occupancy"));
        rr.setDescription (rs.getString     ("description"));
        rr.setEffectiveFrom(rs.getDate      ("effective_from").toLocalDate());
        Date effTo = rs.getDate("effective_to");
        if (effTo != null) rr.setEffectiveTo(effTo.toLocalDate());
        return rr;
    }
}