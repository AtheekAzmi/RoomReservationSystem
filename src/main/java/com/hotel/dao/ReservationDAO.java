package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean save(Reservation r) {
        String sql = "INSERT INTO reservation(reservation_number, guest_id, " +
                "room_id, staff_id, checkin_date, checkout_date, status) " +
                "VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getReservationNumber());
            ps.setInt   (2, r.getGuestId());
            ps.setInt   (3, r.getRoomId());
            ps.setInt   (4, r.getStaffId());
            ps.setDate  (5, Date.valueOf(r.getCheckinDate()));
            ps.setDate  (6, Date.valueOf(r.getCheckoutDate()));
            ps.setString(7, r.getStatus());
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setReservationId(keys.getInt(1));
            return rows > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Reservation findByNumber(String number) {
        String sql = "SELECT res.*, g.guest_name, ro.room_number, s.fullname AS staff_name " +
                "FROM reservation res " +
                "JOIN guest g  ON res.guest_id = g.guest_id " +
                "JOIN room  ro ON res.room_id  = ro.room_id " +
                "JOIN staff s  ON res.staff_id = s.staff_id " +
                "WHERE res.reservation_number=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, number);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Reservation findById(int id) {
        String sql = "SELECT res.*, g.guest_name, ro.room_number, s.fullname AS staff_name " +
                "FROM reservation res " +
                "JOIN guest g  ON res.guest_id = g.guest_id " +
                "JOIN room  ro ON res.room_id  = ro.room_id " +
                "JOIN staff s  ON res.staff_id = s.staff_id " +
                "WHERE res.reservation_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.*, g.guest_name, ro.room_number, s.fullname AS staff_name " +
                "FROM reservation res " +
                "JOIN guest g  ON res.guest_id = g.guest_id " +
                "JOIN room  ro ON res.room_id  = ro.room_id " +
                "JOIN staff s  ON res.staff_id = s.staff_id " +
                "ORDER BY res.created_at DESC";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> findByStatus(String status) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.*, g.guest_name, ro.room_number, s.fullname AS staff_name " +
                "FROM reservation res " +
                "JOIN guest g  ON res.guest_id = g.guest_id " +
                "JOIN room  ro ON res.room_id  = ro.room_id " +
                "JOIN staff s  ON res.staff_id = s.staff_id " +
                "WHERE res.status=? ORDER BY res.checkin_date";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(String reservationNumber, String newStatus) {
        String sql = "UPDATE reservation SET status=? WHERE reservation_number=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, reservationNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int countByStaffId(int staffId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE staff_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean update(Reservation r) {
        String sql = "UPDATE reservation SET room_id=?, checkin_date=?, " +
                "checkout_date=?, status=? WHERE reservation_number=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, r.getRoomId());
            ps.setDate  (2, Date.valueOf(r.getCheckinDate()));
            ps.setDate  (3, Date.valueOf(r.getCheckoutDate()));
            ps.setString(4, r.getStatus());
            ps.setString(5, r.getReservationNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId    (rs.getInt   ("reservation_id"));
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestId          (rs.getInt   ("guest_id"));
        r.setRoomId           (rs.getInt   ("room_id"));
        r.setStaffId          (rs.getInt   ("staff_id"));
        r.setCheckinDate      (rs.getDate  ("checkin_date").toLocalDate());
        r.setCheckoutDate     (rs.getDate  ("checkout_date").toLocalDate());
        r.setStatus           (rs.getString("status"));
        // Joined fields
        try { r.setGuestName (rs.getString("guest_name"));  } catch(SQLException ignored){}
        try { r.setRoomNumber(rs.getString("room_number")); } catch(SQLException ignored){}
        try { r.setStaffName (rs.getString("staff_name"));  } catch(SQLException ignored){}
        return r;
    }
}