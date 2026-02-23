package com.hotel.service;

import com.hotel.config.DatabaseConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.time.LocalDate;

public class ReportService {

    // Report 1: Room Occupancy by Type
    public String getOccupancyReport(String fromDate, String toDate) {
        validateDates(fromDate, toDate);
        JSONArray result = new JSONArray();
        String sql = "SELECT rt.type_name, " +
                "COUNT(r.reservation_id) AS total_bookings, " +
                "SUM(DATEDIFF(r.checkout_date, r.checkin_date)) AS total_nights, " +
                "COUNT(DISTINCT r.room_id) AS rooms_used " +
                "FROM reservation r " +
                "JOIN room ro ON r.room_id = ro.room_id " +
                "JOIN room_type rt ON ro.room_type_id = rt.room_type_id " +
                "WHERE r.checkin_date >= ? AND r.checkout_date <= ? " +
                "AND r.status != 'CANCELLED' " +
                "GROUP BY rt.type_name ORDER BY total_bookings DESC";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(new JSONObject()
                        .put("roomType",      rs.getString("type_name"))
                        .put("totalBookings", rs.getInt   ("total_bookings"))
                        .put("totalNights",   rs.getInt   ("total_nights"))
                        .put("roomsUsed",     rs.getInt   ("rooms_used")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result.toString();
    }

    // Report 2: Daily Revenue
    public String getRevenueReport(String fromDate, String toDate) {
        validateDates(fromDate, toDate);
        JSONArray result = new JSONArray();
        String sql = "SELECT DATE(b.generated_at) AS bill_date, " +
                "COUNT(b.bill_id) AS total_bills, " +
                "SUM(b.subtotal)     AS subtotal, " +
                "SUM(b.tax_amount)   AS tax_total, " +
                "SUM(b.total_amount) AS grand_total " +
                "FROM bill b " +
                "WHERE b.generated_at BETWEEN ? AND ? " +
                "GROUP BY DATE(b.generated_at) " +
                "ORDER BY bill_date";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(new JSONObject()
                        .put("date",       rs.getString    ("bill_date"))
                        .put("totalBills", rs.getInt       ("total_bills"))
                        .put("subtotal",   rs.getBigDecimal("subtotal"))
                        .put("taxTotal",   rs.getBigDecimal("tax_total"))
                        .put("grandTotal", rs.getBigDecimal("grand_total")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result.toString();
    }

    // Report 3: Guest History
    public String getGuestHistoryReport(String fromDate, String toDate) {
        validateDates(fromDate, toDate);
        JSONArray result = new JSONArray();
        String sql = "SELECT g.guest_id, g.guest_name, g.contact_number, " +
                "COUNT(r.reservation_id) AS total_stays, " +
                "SUM(DATEDIFF(r.checkout_date, r.checkin_date)) AS total_nights, " +
                "SUM(b.total_amount) AS total_spent " +
                "FROM guest g " +
                "JOIN reservation r ON g.guest_id = r.guest_id " +
                "LEFT JOIN bill b ON r.reservation_id = b.reservation_id " +
                "WHERE r.checkin_date >= ? AND r.checkin_date <= ? " +
                "AND r.status != 'CANCELLED' " +
                "GROUP BY g.guest_id, g.guest_name, g.contact_number " +
                "ORDER BY total_stays DESC";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(new JSONObject()
                        .put("guestId",       rs.getInt       ("guest_id"))
                        .put("guestName",     rs.getString    ("guest_name"))
                        .put("contactNumber", rs.getString    ("contact_number"))
                        .put("totalStays",    rs.getInt       ("total_stays"))
                        .put("totalNights",   rs.getInt       ("total_nights"))
                        .put("totalSpent",    rs.getBigDecimal("total_spent")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result.toString();
    }

    // Report 4: Staff Activity
    public String getStaffActivityReport(String fromDate, String toDate) {
        validateDates(fromDate, toDate);
        JSONArray result = new JSONArray();
        String sql = "SELECT s.staff_id, s.fullname, s.role, " +
                "COUNT(r.reservation_id) AS reservations_created " +
                "FROM staff s " +
                "LEFT JOIN reservation r ON s.staff_id = r.staff_id " +
                "AND r.created_at BETWEEN ? AND ? " +
                "GROUP BY s.staff_id, s.fullname, s.role " +
                "ORDER BY reservations_created DESC";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(new JSONObject()
                        .put("staffId",              rs.getInt   ("staff_id"))
                        .put("fullName",             rs.getString("fullname"))
                        .put("role",                 rs.getString("role"))
                        .put("reservationsCreated",  rs.getInt   ("reservations_created")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result.toString();
    }

    // Report 5: Reservation Status Summary
    public String getReservationStatusReport() {
        JSONArray result = new JSONArray();
        String sql = "SELECT status, COUNT(*) AS total " +
                "FROM reservation GROUP BY status";
        try (Connection c = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(new JSONObject()
                        .put("status", rs.getString("status"))
                        .put("total",  rs.getInt   ("total")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result.toString();
    }

    private void validateDates(String from, String to) {
        try {
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(to);
            if (t.isBefore(f))
                throw new IllegalArgumentException("End date must be after start date");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format (use YYYY-MM-DD)");
        }
    }
}
//```
//
//        ---
//
//        ## 📋 Build Order Summary
//```
//MODELS FIRST:
//        1. Staff.java
// 2. Guest.java
// 3. RoomType.java
// 4. RoomRate.java
// 5. Room.java
// 6. Reservation.java
// 7. Bill.java
// 8. Payment.java
//
//DAOs SECOND:
//        9.  StaffDAO.java
// 10. GuestDAO.java
// 11. RoomRateDAO.java
// 12. RoomDAO.java
// 13. ReservationDAO.java
// 14. BillDAO.java
// 15. PaymentDAO.java
//
//SERVICES LAST (depend on DAOs):
//        16. AuthService.java
// 17. GuestService.java
// 18. RoomService.java
// 19. ReservationService.java
// 20. BillingService.java
// 21. StaffService.java
// 22. ReportService.java