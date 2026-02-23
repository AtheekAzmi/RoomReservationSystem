package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Guest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    public int save(Guest g) {
        String sql = "INSERT INTO guest(guest_name, address, contact_number, email) " +
                "VALUES(?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getGuestName());
            ps.setString(2, g.getAddress());
            ps.setString(3, g.getContactNumber());
            ps.setString(4, g.getEmail());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public Guest findById(int guestId) {
        String sql = "SELECT * FROM guest WHERE guest_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Guest findByContact(String contactNumber) {
        String sql = "SELECT * FROM guest WHERE contact_number=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Guest> findAll() {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guest ORDER BY guest_name";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Guest> searchByName(String name) {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guest WHERE guest_name LIKE ?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean update(Guest g) {
        String sql = "UPDATE guest SET guest_name=?, address=?, " +
                "contact_number=?, email=? WHERE guest_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getGuestName());
            ps.setString(2, g.getAddress());
            ps.setString(3, g.getContactNumber());
            ps.setString(4, g.getEmail());
            ps.setInt   (5, g.getGuestId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId      (rs.getInt   ("guest_id"));
        g.setGuestName    (rs.getString("guest_name"));
        g.setAddress      (rs.getString("address"));
        g.setContactNumber(rs.getString("contact_number"));
        g.setEmail        (rs.getString("email"));
        return g;
    }
}