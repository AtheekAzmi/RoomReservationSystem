package com.hotel.service;

import com.hotel.dao.StaffDAO;
import com.hotel.model.Staff;
import com.hotel.util.PasswordUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class StaffService {

    private final StaffDAO staffDAO = new StaffDAO();

    public String getAllStaff() {
        List<Staff> list = staffDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Staff s : list) arr.put(toJson(s));
        return arr.toString();
    }

    public String createStaff(JSONObject json) {
        String username = json.optString("username", "").trim();
        String password = json.optString("password", "").trim();
        String fullName = json.optString("fullName",  "").trim();
        String email    = json.optString("email",     "").trim();
        String role     = json.optString("role",      "").trim();

        // Validate
        if (username.length() < 3 || username.length() > 50)
            throw new IllegalArgumentException("Username must be 3-50 characters");
        if (!username.matches("^[a-zA-Z0-9_]+$"))
            throw new IllegalArgumentException("Username can only contain letters, numbers and underscore");
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (!password.matches(".*\\d.*"))
            throw new IllegalArgumentException("Password must contain at least one digit");
        if (fullName.length() < 2)
            throw new IllegalArgumentException("Full name is required");
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid email address");
        if (!List.of("admin","receptionist").contains(role))
            throw new IllegalArgumentException("Role must be admin or receptionist");
        if (staffDAO.usernameExists(username))
            throw new IllegalArgumentException("Username already exists");

        Staff s = new Staff(username, PasswordUtil.hash(password), fullName, email, role);
        boolean ok = staffDAO.save(s);
        if (!ok) throw new RuntimeException("Failed to create staff");
        return new JSONObject().put("message", "Staff created successfully").toString();
    }

    public String updateStaff(int staffId, JSONObject json) {
        Staff s = staffDAO.findById(staffId);
        if (s == null) throw new IllegalArgumentException("Staff not found");

        String fullName = json.optString("fullName", s.getFullName()).trim();
        String email    = json.optString("email",    s.getEmail()).trim();
        String role     = json.optString("role",     s.getRole()).trim();

        if (fullName.length() < 2)
            throw new IllegalArgumentException("Full name is required");
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid email");
        if (!List.of("admin","receptionist").contains(role))
            throw new IllegalArgumentException("Invalid role");

        s.setFullName(fullName);
        s.setEmail   (email);
        s.setRole    (role);

        boolean ok = staffDAO.update(s);
        if (!ok) throw new RuntimeException("Update failed");
        return new JSONObject().put("message", "Staff updated").toString();
    }

    public String deleteStaff(int staffId) {
        Staff s = staffDAO.findById(staffId);
        if (s == null) throw new IllegalArgumentException("Staff not found");
        boolean ok = staffDAO.delete(staffId);
        if (!ok) throw new RuntimeException("Delete failed");
        return new JSONObject().put("message", "Staff deleted").toString();
    }

    private JSONObject toJson(Staff s) {
        return new JSONObject()
                .put("staffId",  s.getStaffId())
                .put("username", s.getUsername())
                .put("fullName", s.getFullName())
                .put("email",    s.getEmail())
                .put("role",     s.getRole());
        // Note: never include passwordHash in JSON response
    }
}