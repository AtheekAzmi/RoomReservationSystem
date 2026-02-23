package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public boolean save(Staff s) {
        String sql = "INSERT INTO staff(username, password_hash, fullname, email, role) " +
                "VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getUsername());
            ps.setString(2, s.getPasswordHash());
            ps.setString(3, s.getFullName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Staff findByUsername(String username) {
        String sql = "SELECT * FROM staff WHERE username = ?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Staff findById(int staffId) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Staff> findAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY fullname";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean update(Staff s) {
        String sql = "UPDATE staff SET fullname=?, email=?, role=? WHERE staff_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getRole());
            ps.setInt   (4, s.getStaffId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updatePassword(int staffId, String newHash) {
        String sql = "UPDATE staff SET password_hash=? WHERE staff_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt   (2, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int staffId) {
        String sql = "DELETE FROM staff WHERE staff_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM staff WHERE username=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId     (rs.getInt   ("staff_id"));
        s.setUsername    (rs.getString("username"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setFullName    (rs.getString("fullname"));
        s.setEmail       (rs.getString("email"));
        s.setRole        (rs.getString("role"));
        return s;
    }
}