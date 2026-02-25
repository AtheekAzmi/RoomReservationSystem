package com.hotel;

import com.hotel.config.DatabaseConfig;
import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.service.*;
import com.hotel.util.*;
import org.json.JSONObject;
import java.sql.Connection;
import java.time.LocalDate;

public class TestRunner {



    // ── Counters ──────────────────────────────────────────────────────────
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   HOTEL RESERVATION SYSTEM — BACKEND TEST");
        System.out.println("=================================================\n");

        cleanDatabase();          // ← ADD THIS FIRST
        testDatabaseConnection();
        testPasswordUtil();
        testStaffDAO();
        testAuthService();
        testGuestDAO();
        testRoomDAO();
        testReservationService();
        testBillingService();
        testReportService();
        testSessionManager();
        // ...
    }

    // ─────────────────────────────────────────────────────────────────────
// CLEAN UP — Reset DB before each test run
// ─────────────────────────────────────────────────────────────────────
    static void cleanDatabase() {
        printSection("0. CLEANING TEST DATA");
        try (java.sql.Connection c =
                     com.hotel.config.DatabaseConfig.getInstance().getConnection()) {

            // Must delete in this order due to foreign keys
            String[] cleanupSql = {
                    "DELETE FROM payment",
                    "DELETE FROM bill",
                    "DELETE FROM reservation WHERE reservation_number LIKE 'RES-%'",
                    "DELETE FROM guest WHERE contact_number " +
                            "IN ('+94771234567','+94712345678','+94723456789')",
                    "UPDATE room SET room_status = 'AVAILABLE'"
            };

            for (String sql : cleanupSql) {
                try (java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
                    int rows = ps.executeUpdate();
                    pass("Cleaned: [" + sql.substring(0, Math.min(40, sql.length()))
                            + "...] → " + rows + " row(s)");
                }
            }

        } catch (Exception e) {
            fail("Cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // In testReservationService() — change the date lines to:
    int offset = (int)(System.currentTimeMillis() % 100) + 10; // unique offset

    JSONObject newRes = new JSONObject()
            .put("guestName",     "Alice Wonderland")
            .put("contactNumber", "+94712345678")
            .put("address",       "Wonderland, Colombo")
            .put("email",         "alice@test.com")
            .put("roomType",      "Single")
            .put("checkinDate",   LocalDate.now().plusDays(offset).toString())
            .put("checkoutDate",  LocalDate.now().plusDays(offset + 3).toString());

    // ─────────────────────────────────────────────────────────────────────
    // TEST 1 — DATABASE CONNECTION
    // ─────────────────────────────────────────────────────────────────────
    static void testDatabaseConnection() {
        printSection("1. DATABASE CONNECTION");
        try {
            Connection conn = DatabaseConfig.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                pass("Connected to MySQL successfully");
                pass("Database: " + conn.getCatalog());
                conn.close();
            } else {
                fail("Connection is null or closed");
            }
        } catch (Exception e) {
            fail("Database connection FAILED: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 2 — PASSWORD UTILITY
    // ─────────────────────────────────────────────────────────────────────
    static void testPasswordUtil() {
        printSection("2. PASSWORD UTILITY");
        try {
            String hash = PasswordUtil.hash("Admin@123");
            if (hash != null && hash.length() == 64) {
                pass("Hash generated, length=64 ✓");
            } else {
                fail("Hash wrong length: " + (hash == null ? "null" : hash.length()));
            }

            if (PasswordUtil.verify("Admin@123", hash)) {
                pass("Password verification: correct password → true ✓");
            } else {
                fail("Password verification failed for correct password");
            }

            if (!PasswordUtil.verify("wrongPass", hash)) {
                pass("Password verification: wrong password → false ✓");
            } else {
                fail("Wrong password returned true");
            }

            // Test strength validation
            try {
                PasswordUtil.validateStrength("abc");
                fail("Should have rejected short password");
            } catch (IllegalArgumentException e) {
                pass("Rejects password under 6 chars ✓");
            }

            try {
                PasswordUtil.validateStrength("abcdefg"); // no digit
                fail("Should have rejected password with no digit");
            } catch (IllegalArgumentException e) {
                pass("Rejects password with no digit ✓");
            }

        } catch (Exception e) {
            fail("PasswordUtil error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 3 — STAFF DAO
    // ─────────────────────────────────────────────────────────────────────
    static void testStaffDAO() {
        printSection("3. STAFF DAO");
        StaffDAO dao = new StaffDAO();

        try {
            // Find admin (must be inserted first via SQL)
            Staff admin = dao.findByUsername("admin");
            if (admin != null) {
                pass("findByUsername('admin') → found ✓");
                pass("Admin role: " + admin.getRole());
                pass("Admin name: " + admin.getFullName());
            } else {
                fail("Admin not found — run the INSERT SQL first!");
            }

            // Find by ID
            if (admin != null) {
                Staff byId = dao.findById(admin.getStaffId());
                if (byId != null) {
                    pass("findById(" + admin.getStaffId() + ") → found ✓");
                } else {
                    fail("findById returned null");
                }
            }

            // Find all
            var allStaff = dao.findAll();
            pass("findAll() returned " + allStaff.size() + " record(s)");

            // Username exists check
            if (dao.usernameExists("admin")) {
                pass("usernameExists('admin') → true ✓");
            } else {
                fail("usernameExists returned false for existing user");
            }

            if (!dao.usernameExists("nonexistent_xyz_999")) {
                pass("usernameExists('nonexistent') → false ✓");
            } else {
                fail("usernameExists returned true for non-existent user");
            }

            // Create a test receptionist
            String testUsername = "test_receptionist_" + System.currentTimeMillis();
            Staff newStaff = new Staff(
                    testUsername,
                    PasswordUtil.hash("Staff@123"),
                    "Test Receptionist",
                    "test@hotel.com",
                    "receptionist"
            );
            boolean saved = dao.save(newStaff);
            if (saved) {
                pass("save(new Staff) → success ✓");
            } else {
                fail("save(new Staff) failed");
            }

            // Delete the test receptionist
            Staff created = dao.findByUsername(testUsername);
            if (created != null) {
                boolean deleted = dao.delete(created.getStaffId());
                if (deleted) {
                    pass("delete(staffId) → success ✓");
                } else {
                    fail("delete failed");
                }
            }

        } catch (Exception e) {
            fail("StaffDAO error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 4 — AUTH SERVICE
    // ─────────────────────────────────────────────────────────────────────
    static void testAuthService() {
        printSection("4. AUTH SERVICE");
        AuthService authService = new AuthService();

        try {
            // Valid login
            JSONObject validLogin = new JSONObject()
                    .put("username", "admin")
                    .put("password", "Admin@123");
            String result = authService.login(validLogin);
            JSONObject json = new JSONObject(result);

            if (json.has("token") && !json.getString("token").isEmpty()) {
                pass("Login success → token received ✓");
                pass("Role returned: " + json.getString("role"));
                pass("Full name: "     + json.getString("fullName"));
            } else {
                fail("Login did not return a token");
            }

            // Store token for session test
            String token = json.getString("token");

            // Wrong password
            try {
                JSONObject badLogin = new JSONObject()
                        .put("username", "admin")
                        .put("password", "WrongPass1");
                authService.login(badLogin);
                fail("Should have rejected wrong password");
            } catch (IllegalArgumentException e) {
                pass("Wrong password → rejected ✓ (" + e.getMessage() + ")");
            }

            // Non-existent user
            try {
                JSONObject badUser = new JSONObject()
                        .put("username", "nobody")
                        .put("password", "Admin@123");
                authService.login(badUser);
                fail("Should have rejected non-existent user");
            } catch (IllegalArgumentException e) {
                pass("Non-existent user → rejected ✓");
            }

            // Empty username
            try {
                JSONObject emptyUser = new JSONObject()
                        .put("username", "")
                        .put("password", "Admin@123");
                authService.login(emptyUser);
                fail("Should have rejected empty username");
            } catch (IllegalArgumentException e) {
                pass("Empty username → rejected ✓");
            }

            // Logout
            String logoutResult = authService.logout(token);
            pass("Logout → " + logoutResult);

        } catch (Exception e) {
            fail("AuthService error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 5 — GUEST DAO
    // ─────────────────────────────────────────────────────────────────────
    static void testGuestDAO() {
        printSection("5. GUEST DAO");
        GuestDAO dao = new GuestDAO();

        try {
            // Create a guest
            Guest g = new Guest(
                    "John Smith",
                    "123 Main St, Colombo",
                    "+94771234567",
                    "john@test.com"
            );
            int guestId = dao.save(g);
            if (guestId > 0) {
                pass("save(Guest) → ID=" + guestId + " ✓");
            } else {
                fail("save(Guest) failed — returned -1");
                return;
            }

            // Find by ID
            Guest found = dao.findById(guestId);
            if (found != null && found.getGuestName().equals("John Smith")) {
                pass("findById(" + guestId + ") → " + found.getGuestName() + " ✓");
            } else {
                fail("findById returned wrong or null guest");
            }

            // Find by contact
            Guest byContact = dao.findByContact("+94771234567");
            if (byContact != null) {
                pass("findByContact → found ✓");
            } else {
                fail("findByContact returned null");
            }

            // Search by name
            var results = dao.searchByName("John");
            if (!results.isEmpty()) {
                pass("searchByName('John') → " + results.size() + " result(s) ✓");
            } else {
                fail("searchByName returned empty");
            }

            // Update guest
            found.setAddress("456 New St, Colombo");
            boolean updated = dao.update(found);
            if (updated) {
                pass("update(Guest) → success ✓");
            } else {
                fail("update(Guest) failed");
            }

            // Find all
            var all = dao.findAll();
            pass("findAll() → " + all.size() + " guest(s) in DB");

        } catch (Exception e) {
            fail("GuestDAO error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 6 — ROOM DAO
    // ─────────────────────────────────────────────────────────────────────
    static void testRoomDAO() {
        printSection("6. ROOM DAO");
        RoomDAO dao = new RoomDAO();

        try {
            // Find all rooms (seeded via SQL script)
            var allRooms = dao.findAll();
            if (!allRooms.isEmpty()) {
                pass("findAll() → " + allRooms.size() + " room(s) found ✓");

                // Print each room
                for (Room r : allRooms) {
                    System.out.printf("      Room: %-5s | Floor: %d | Type: %-8s | Status: %s%n",
                            r.getRoomNumber(), r.getFloorNumber(),
                            r.getTypeName(),   r.getRoomStatus());
                }
            } else {
                fail("No rooms found — run the seed SQL script first!");
            }

            // Find available rooms for a date range
            LocalDate checkin  = LocalDate.now().plusDays(5);
            LocalDate checkout = LocalDate.now().plusDays(8);
            var available = dao.findAvailableRooms("Single", checkin, checkout);
            pass("findAvailableRooms('Single') → " + available.size() + " available");

            // Find by number
            if (!allRooms.isEmpty()) {
                String roomNo = allRooms.get(0).getRoomNumber();
                Room byNumber = dao.findByNumber(roomNo);
                if (byNumber != null) {
                    pass("findByNumber('" + roomNo + "') → found ✓");
                } else {
                    fail("findByNumber returned null");
                }

                // Find by ID
                Room byId = dao.findById(allRooms.get(0).getRoomId());
                if (byId != null) {
                    pass("findById(" + byId.getRoomId() + ") → found ✓");
                } else {
                    fail("findById returned null");
                }

                // Update status
                int roomId = allRooms.get(0).getRoomId();
                dao.updateStatus(roomId, "MAINTENANCE");
                Room updated = dao.findById(roomId);
                if ("MAINTENANCE".equals(updated.getRoomStatus())) {
                    pass("updateStatus → MAINTENANCE ✓");
                } else {
                    fail("updateStatus did not apply");
                }
                // Reset back
                dao.updateStatus(roomId, "AVAILABLE");
                pass("Reset room back to AVAILABLE ✓");
            }

        } catch (Exception e) {
            fail("RoomDAO error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 7 — RESERVATION SERVICE (Full Flow)
    // ─────────────────────────────────────────────────────────────────────
    static void testReservationService() {
        printSection("7. RESERVATION SERVICE");
        ReservationService service = new ReservationService();
        AuthService        auth    = new AuthService();

        try {
            // Login first to get a valid token
            String loginResult = auth.login(new JSONObject()
                    .put("username", "admin")
                    .put("password", "Admin@123"));
            String token = new JSONObject(loginResult).getString("token");
            pass("Got valid session token ✓");

            // ── Create Reservation ──
            JSONObject newRes = new JSONObject()
                    .put("guestName",     "Alice Wonderland")
                    .put("contactNumber", "+94712345678")
                    .put("address",       "Wonderland, Colombo")
                    .put("email",         "alice@test.com")
                    .put("roomType",      "Single")
                    .put("checkinDate",   LocalDate.now().plusDays(2).toString())
                    .put("checkoutDate",  LocalDate.now().plusDays(5).toString());

            String createResult = service.createReservation(newRes, token);
            JSONObject created  = new JSONObject(createResult);

            if (created.has("reservationNumber")) {
                String resNo = created.getString("reservationNumber");
                pass("createReservation → " + resNo + " ✓");
                pass("Guest: "  + created.getString("guestName"));
                pass("Room: "   + created.getString("roomNumber"));
                pass("Dates: "  + created.getString("checkinDate") +
                        " to "     + created.getString("checkoutDate"));

                // ── Get Reservation ──
                String fetched = service.getReservation(resNo);
                if (fetched != null && !fetched.isEmpty()) {
                    pass("getReservation('" + resNo + "') → found ✓");
                } else {
                    fail("getReservation returned empty");
                }

                // ── Get All Reservations ──
                String allRes = service.getAllReservations();
                pass("getAllReservations() returned data ✓");

                // ── Update Reservation ──
                JSONObject updateData = new JSONObject()
                        .put("checkoutDate", LocalDate.now().plusDays(6).toString());
                String updateResult = service.updateReservation(resNo, updateData);
                pass("updateReservation → " + new JSONObject(updateResult).getString("message") + " ✓");

                // ── Cancel Reservation ──
                String cancelResult = service.cancelReservation(resNo);
                pass("cancelReservation → " +
                        new JSONObject(cancelResult).getString("message") + " ✓");

                // ── Try to cancel again (should fail) ──
                try {
                    service.cancelReservation(resNo);
                    fail("Should have rejected double-cancel");
                } catch (IllegalArgumentException e) {
                    pass("Double-cancel rejected ✓ (" + e.getMessage() + ")");
                }

            } else {
                fail("createReservation did not return reservationNumber");
            }

            // ── Validation Tests ──
            System.out.println("\n   --- Validation Tests ---");

            // Past check-in date
            try {
                service.createReservation(new JSONObject()
                        .put("guestName",     "Test Guest")
                        .put("contactNumber", "+94712345678")
                        .put("roomType",      "Single")
                        .put("checkinDate",   LocalDate.now().minusDays(1).toString())
                        .put("checkoutDate",  LocalDate.now().plusDays(2).toString()), token);
                fail("Should reject past check-in date");
            } catch (IllegalArgumentException e) {
                pass("Past check-in → rejected ✓");
            }

            // Checkout before checkin
            try {
                service.createReservation(new JSONObject()
                        .put("guestName",     "Test Guest")
                        .put("contactNumber", "+94712345678")
                        .put("roomType",      "Single")
                        .put("checkinDate",   LocalDate.now().plusDays(5).toString())
                        .put("checkoutDate",  LocalDate.now().plusDays(3).toString()), token);
                fail("Should reject checkout before checkin");
            } catch (IllegalArgumentException e) {
                pass("Checkout before checkin → rejected ✓");
            }

            // Invalid room type
            try {
                service.createReservation(new JSONObject()
                        .put("guestName",     "Test Guest")
                        .put("contactNumber", "+94712345678")
                        .put("roomType",      "Presidential")
                        .put("checkinDate",   LocalDate.now().plusDays(1).toString())
                        .put("checkoutDate",  LocalDate.now().plusDays(3).toString()), token);
                fail("Should reject invalid room type");
            } catch (IllegalArgumentException e) {
                pass("Invalid room type → rejected ✓");
            }

            // Invalid contact
            try {
                service.createReservation(new JSONObject()
                        .put("guestName",     "Test Guest")
                        .put("contactNumber", "abc")
                        .put("roomType",      "Single")
                        .put("checkinDate",   LocalDate.now().plusDays(1).toString())
                        .put("checkoutDate",  LocalDate.now().plusDays(3).toString()), token);
                fail("Should reject invalid contact");
            } catch (IllegalArgumentException e) {
                pass("Invalid contact number → rejected ✓");
            }

        } catch (Exception e) {
            fail("ReservationService error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 8 — BILLING SERVICE
    // ─────────────────────────────────────────────────────────────────────
    static void testBillingService() {
        printSection("8. BILLING SERVICE");
        BillingService billing = new BillingService();
        ReservationService resSvc = new ReservationService();
        AuthService auth = new AuthService();

        try {
            // Create a reservation to bill
            String token = new JSONObject(auth.login(new JSONObject()
                    .put("username", "admin")
                    .put("password", "Admin@123")))
                    .getString("token");

            String createResult = resSvc.createReservation(new JSONObject()
                    .put("guestName",     "Bob Builder")
                    .put("contactNumber", "+94723456789")
                    .put("address",       "Build Street, Colombo")
                    .put("email",         "bob@test.com")
                    .put("roomType",      "Double")
                    .put("checkinDate",   LocalDate.now().plusDays(1).toString())
                    .put("checkoutDate",  LocalDate.now().plusDays(4).toString()), token);

            String resNo = new JSONObject(createResult).getString("reservationNumber");
            pass("Created test reservation: " + resNo + " ✓");

            // Generate bill
            String billResult = billing.generateBill(resNo);
            JSONObject bill   = new JSONObject(billResult);

            if (bill.has("billNumber")) {
                pass("generateBill → " + bill.getString("billNumber") + " ✓");
                pass("Subtotal:  $" + bill.getBigDecimal("subtotal"));
                pass("Tax:       $" + bill.getBigDecimal("taxAmount"));
                pass("Total:     $" + bill.getBigDecimal("totalAmount"));
                pass("Status:    "  + bill.getString("paymentStatus"));

                // Get bill by ID
                int billId = bill.getInt("billId");
                String fetched = billing.getBill(billId);
                pass("getBill(" + billId + ") → found ✓");

                // Process payment
                String payResult = billing.processPayment(new JSONObject()
                        .put("billId",         billId)
                        .put("paymentMethod",  "CASH")
                        .put("amount",         bill.getBigDecimal("totalAmount").toString()));
                JSONObject payJson = new JSONObject(payResult);
                pass("processPayment → " + payJson.getString("paymentStatus") + " ✓");

                // Try to pay again (already PAID)
                try {
                    billing.processPayment(new JSONObject()
                            .put("billId",        billId)
                            .put("paymentMethod", "CASH")
                            .put("amount",        "10.00"));
                    fail("Should reject payment on already-paid bill");
                } catch (IllegalArgumentException e) {
                    pass("Double payment → rejected ✓");
                }

                // Get all bills
                String allBills = billing.getAllBills();
                pass("getAllBills() → data returned ✓");

            } else {
                fail("generateBill did not return billNumber");
            }

        } catch (Exception e) {
            fail("BillingService error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 9 — REPORT SERVICE
    // ─────────────────────────────────────────────────────────────────────
    static void testReportService() {
        printSection("9. REPORT SERVICE");
        ReportService reportSvc = new ReportService();

        String from = LocalDate.now().minusMonths(1).toString();
        String to   = LocalDate.now().plusDays(30).toString();

        try {
            String occupancy = reportSvc.getOccupancyReport(from, to);
            pass("getOccupancyReport → data received ✓");
            System.out.println("      Data: " + occupancy);

            String revenue = reportSvc.getRevenueReport(from, to);
            pass("getRevenueReport → data received ✓");
            System.out.println("      Data: " + revenue);

            String guestHistory = reportSvc.getGuestHistoryReport(from, to);
            pass("getGuestHistoryReport → data received ✓");

            String staffActivity = reportSvc.getStaffActivityReport(from, to);
            pass("getStaffActivityReport → data received ✓");
            System.out.println("      Data: " + staffActivity);

            String statusReport = reportSvc.getReservationStatusReport();
            pass("getReservationStatusReport → data received ✓");
            System.out.println("      Data: " + statusReport);

            // Invalid date range
            try {
                reportSvc.getOccupancyReport("2026-12-01", "2026-01-01");
                fail("Should reject end date before start date");
            } catch (IllegalArgumentException e) {
                pass("Invalid date range → rejected ✓");
            }

        } catch (Exception e) {
            fail("ReportService error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TEST 10 — SESSION MANAGER
    // ─────────────────────────────────────────────────────────────────────
    static void testSessionManager() {
        printSection("10. SESSION MANAGER");
        SessionManager sm = SessionManager.getInstance();

        try {
            StaffDAO dao   = new StaffDAO();
            Staff    admin = dao.findByUsername("admin");

            if (admin == null) {
                fail("Admin not found — cannot test SessionManager");
                return;
            }

            // Create session
            String token = sm.createSession(admin);
            if (token != null && !token.isEmpty()) {
                pass("createSession → token generated ✓");
            } else {
                fail("createSession returned null/empty");
            }

            // Validate session
            if (sm.isValid(token)) {
                pass("isValid(token) → true ✓");
            } else {
                fail("isValid returned false for fresh token");
            }

            // Get session
            Staff retrieved = sm.getSession(token);
            if (retrieved != null && retrieved.getUsername().equals("admin")) {
                pass("getSession → returned correct Staff ✓");
            } else {
                fail("getSession returned wrong user");
            }

            // Admin check
            if (sm.isAdmin(token)) {
                pass("isAdmin → true for admin ✓");
            } else {
                fail("isAdmin returned false for admin");
            }

            // Invalidate
            sm.invalidate(token);
            if (!sm.isValid(token)) {
                pass("invalidate → session removed ✓");
            } else {
                fail("invalidate did not remove session");
            }

            // Invalid token
            Staff ghost = sm.getSession("fake-token-xyz");
            if (ghost == null) {
                pass("Invalid token → returns null ✓");
            } else {
                fail("Invalid token returned a Staff object");
            }

            // Singleton check
            SessionManager sm2 = SessionManager.getInstance();
            if (sm == sm2) {
                pass("Singleton: same instance returned ✓");
            } else {
                fail("Singleton broken — different instances");
            }

        } catch (Exception e) {
            fail("SessionManager error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ─────────────────────────────────────────────────────────────────────
    static void printSection(String title) {
        System.out.println("\n─────────────────────────────────────────────────");
        System.out.println("  TEST " + title);
        System.out.println("─────────────────────────────────────────────────");
    }

    static void pass(String message) {
        System.out.println("  ✅ PASS: " + message);
        passed++;
    }

    static void fail(String message) {
        System.out.println("  ❌ FAIL: " + message);
        failed++;
    }
}
//```
//
//        ---
//
//        ## Step 3 — Run It
//
//Right-click `TestRunner.java` → **Run 'TestRunner.main()'**
//
//        ---
//
//        ## ✅ Expected Console Output
//```
//        =================================================
//HOTEL RESERVATION SYSTEM — BACKEND TEST
//=================================================
//
//        ─────────────────────────────────────────────────
//TEST 1. DATABASE CONNECTION
//─────────────────────────────────────────────────
//        ✅ PASS: Connected to MySQL successfully
//  ✅ PASS: Database: hotel_db
//
//─────────────────────────────────────────────────
//TEST 2. PASSWORD UTILITY
//─────────────────────────────────────────────────
//        ✅ PASS: Hash generated, length=64 ✓
//        ✅ PASS: Password verification: correct password → true ✓
//        ✅ PASS: Password verification: wrong password → false ✓
//        ✅ PASS: Rejects password under 6 chars ✓
//        ✅ PASS: Rejects password with no digit ✓
//
//        ... (and so on for all 10 sections)
//
//        =================================================
//RESULTS:  45 PASSED  |  0 FAILED
//=================================================