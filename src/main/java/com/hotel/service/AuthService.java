package com.hotel.service;

import com.hotel.dao.StaffDAO;
import com.hotel.model.Staff;
import com.hotel.util.PasswordUtil;
import com.hotel.util.SessionManager;
import org.json.JSONObject;

public class AuthService {

    private final StaffDAO staffDAO = new StaffDAO();

    public String login(JSONObject json) {
        String username = json.optString("username", "").trim();
        String password = json.optString("password", "").trim();

        // Validate input
        if (username.isEmpty())
            throw new IllegalArgumentException("Username is required");
        if (password.isEmpty())
            throw new IllegalArgumentException("Password is required");
        if (username.length() < 3)
            throw new IllegalArgumentException("Invalid username");
        if (password.length() < 6)
            throw new IllegalArgumentException("Invalid password");

        // Fetch staff
        Staff staff = staffDAO.findByUsername(username);
        if (staff == null)
            throw new IllegalArgumentException("Invalid username or password");

        // Verify password
        if (!PasswordUtil.verify(password, staff.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username or password");

        // Create session
        String token = SessionManager.getInstance().createSession(staff);

        return new JSONObject()
                .put("token",    token)
                .put("role",     staff.getRole())
                .put("fullName", staff.getFullName())
                .put("staffId",  staff.getStaffId())
                .toString();
    }

    public String logout(String token) {
        SessionManager.getInstance().invalidate(token);
        return new JSONObject().put("message", "Logged out successfully").toString();
    }
}