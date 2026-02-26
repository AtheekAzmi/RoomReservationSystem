package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public boolean save(Payment p) {
        String sql = "INSERT INTO payment(bill_id, amount_paid, discount_amount, " +
                "payment_method, payment_status) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt       (1, p.getBillId());
            ps.setBigDecimal(2, p.getAmountPaid());
            ps.setBigDecimal(3, p.getDiscountAmount() != null ? p.getDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setString    (4, p.getPaymentMethod());
            ps.setString    (5, p.getPaymentStatus());
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setPaymentId(keys.getInt(1));
            return rows > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Payment> findByBillId(int billId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE bill_id=? ORDER BY payment_date";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId     (rs.getInt        ("payment_id"));
        p.setBillId        (rs.getInt        ("bill_id"));
        p.setAmountPaid    (rs.getBigDecimal ("amount_paid"));
        p.setDiscountAmount(rs.getBigDecimal ("discount_amount"));
        p.setPaymentMethod (rs.getString     ("payment_method"));
        p.setPaymentStatus (rs.getString     ("payment_status"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        return p;
    }
}