package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Bill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public boolean save(Bill b) {
        String sql = "INSERT INTO bill(bill_number, reservation_id, subtotal, " +
                "tax_rate, tax_amount, discount_amount, total_amount, payment_status) " +
                "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString    (1, b.getBillNumber());
            ps.setInt       (2, b.getReservationId());
            ps.setBigDecimal(3, b.getSubtotal());
            ps.setBigDecimal(4, b.getTaxRate());
            ps.setBigDecimal(5, b.getTaxAmount());
            ps.setBigDecimal(6, b.getDiscountAmount());
            ps.setBigDecimal(7, b.getTotalAmount());
            ps.setString    (8, b.getPaymentStatus());
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) b.setBillId(keys.getInt(1));
            return rows > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Bill findById(int billId) {
        String sql = "SELECT b.*, res.reservation_number, g.guest_name " +
                "FROM bill b " +
                "JOIN reservation res ON b.reservation_id = res.reservation_id " +
                "JOIN guest g ON res.guest_id = g.guest_id " +
                "WHERE b.bill_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Bill findByReservationId(int reservationId) {
        String sql = "SELECT b.*, res.reservation_number, g.guest_name " +
                "FROM bill b " +
                "JOIN reservation res ON b.reservation_id = res.reservation_id " +
                "JOIN guest g ON res.guest_id = g.guest_id " +
                "WHERE b.reservation_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, res.reservation_number, g.guest_name " +
                "FROM bill b " +
                "JOIN reservation res ON b.reservation_id = res.reservation_id " +
                "JOIN guest g ON res.guest_id = g.guest_id " +
                "ORDER BY b.generated_at DESC";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateAdjustments(int billId, java.math.BigDecimal discountAmount,
                                      java.math.BigDecimal taxRate,
                                      java.math.BigDecimal taxAmount,
                                      java.math.BigDecimal totalAmount) {
        String sql = "UPDATE bill SET discount_amount=?, tax_rate=?, tax_amount=?, total_amount=? WHERE bill_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, discountAmount);
            ps.setBigDecimal(2, taxRate);
            ps.setBigDecimal(3, taxAmount);
            ps.setBigDecimal(4, totalAmount);
            ps.setInt       (5, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updatePaymentStatus(int billId, String status) {
        String sql = "UPDATE bill SET payment_status=? WHERE bill_id=?";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId        (rs.getInt        ("bill_id"));
        b.setBillNumber    (rs.getString     ("bill_number"));
        b.setReservationId (rs.getInt        ("reservation_id"));
        b.setSubtotal      (rs.getBigDecimal ("subtotal"));
        b.setTaxRate       (rs.getBigDecimal ("tax_rate"));
        b.setTaxAmount     (rs.getBigDecimal ("tax_amount"));
        b.setDiscountAmount(rs.getBigDecimal ("discount_amount"));
        b.setTotalAmount   (rs.getBigDecimal ("total_amount"));
        b.setPaymentStatus (rs.getString     ("payment_status"));
        Timestamp ts = rs.getTimestamp("generated_at");
        if (ts != null) b.setGeneratedAt(ts.toLocalDateTime());
        try { b.setReservationNumber(rs.getString("reservation_number")); } catch(SQLException ignored){}
        try { b.setGuestName        (rs.getString("guest_name"));         } catch(SQLException ignored){}
        return b;
    }
}