package com.hotel.service;

import com.hotel.dao.GuestDAO;
import com.hotel.model.Guest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class GuestService {

    private final GuestDAO guestDAO = new GuestDAO();

    public String getAllGuests() {
        List<Guest> list = guestDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Guest g : list) arr.put(toJson(g));
        return arr.toString();
    }

    public String getGuest(int guestId) {
        Guest g = guestDAO.findById(guestId);
        if (g == null) throw new IllegalArgumentException("Guest not found");
        return toJson(g).toString();
    }

    // Find existing guest or create new one
    public Guest findOrCreate(String guestName, String contactNumber,
                              String address, String email) {
        Guest existing = guestDAO.findByContact(contactNumber);
        if (existing != null) return existing;

        Guest g = new Guest(guestName, address, contactNumber, email);
        int id = guestDAO.save(g);
        if (id == -1) throw new RuntimeException("Failed to create guest");
        g.setGuestId(id);
        return g;
    }

    public String createGuest(JSONObject json) {
        String name    = json.optString("guestName",     "").trim();
        String contact = json.optString("contactNumber", "").trim();
        String address = json.optString("address",       "").trim();
        String email   = json.optString("email",         "").trim();

        // Validate
        if (name.isEmpty() || name.length() < 2)
            throw new IllegalArgumentException("Guest name must be at least 2 characters");
        if (!contact.matches("^[0-9+\\-]{7,15}$"))
            throw new IllegalArgumentException("Invalid contact number format");
        if (!email.isEmpty() && !email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid email format");

        Guest g = findOrCreate(name, contact, address, email);
        return toJson(g).toString();
    }

    public String updateGuest(int guestId, JSONObject json) {
        Guest g = guestDAO.findById(guestId);
        if (g == null) throw new IllegalArgumentException("Guest not found");

        String name    = json.optString("guestName",     g.getGuestName());
        String contact = json.optString("contactNumber", g.getContactNumber());
        String address = json.optString("address",       g.getAddress());
        String email   = json.optString("email",         g.getEmail());

        if (name.length() < 2)
            throw new IllegalArgumentException("Guest name too short");
        if (!contact.matches("^[0-9+\\-]{7,15}$"))
            throw new IllegalArgumentException("Invalid contact number");

        g.setGuestName    (name);
        g.setContactNumber(contact);
        g.setAddress      (address);
        g.setEmail        (email);

        boolean ok = guestDAO.update(g);
        if (!ok) throw new RuntimeException("Update failed");
        return toJson(g).toString();
    }

    private JSONObject toJson(Guest g) {
        return new JSONObject()
                .put("guestId",       g.getGuestId())
                .put("guestName",     g.getGuestName())
                .put("address",       g.getAddress() != null ? g.getAddress() : "")
                .put("contactNumber", g.getContactNumber())
                .put("email",         g.getEmail() != null ? g.getEmail() : "");
    }
}