package com.hotel.service;

import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.*;
import com.hotel.observer.NotificationService;
import com.hotel.observer.ReservationEvent;
import com.hotel.util.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ReservationService {

    private final ReservationDAO resDAO   = new ReservationDAO();
    private final RoomDAO        roomDAO  = new RoomDAO();
    private final GuestService   guestSvc = new GuestService();
    private final RoomService    roomSvc  = new RoomService();

    public String getAllReservations() {
        List<Reservation> list = resDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Reservation r : list) arr.put(toJson(r));
        return arr.toString();
    }

    public String getReservation(String number) {
        Reservation r = resDAO.findByNumber(number);
        if (r == null) throw new IllegalArgumentException("Reservation not found: " + number);
        return toJson(r).toString();
    }

    public String createReservation(JSONObject json, String token) {
        // 1. Get staff from session
        Staff staff = SessionManager.getInstance().getSession(token);
        if (staff == null) throw new IllegalArgumentException("Invalid session");

        // 2. Validate guest fields
        String guestName = json.optString("guestName", "").trim();
        String contact   = json.optString("contactNumber", "").trim();
        String address   = json.optString("address", "").trim();
        String email     = json.optString("email", "").trim();

        if (guestName.length() < 2)
            throw new IllegalArgumentException("Guest name must be at least 2 characters");
        if (!contact.matches("^[0-9+\\-]{7,15}$"))
            throw new IllegalArgumentException("Invalid contact number (7-15 digits)");
        if (!email.isEmpty() && !email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid email address");

        // 3. Validate dates
        String checkinStr  = json.optString("checkinDate", "");
        String checkoutStr = json.optString("checkoutDate", "");

        if (checkinStr.isEmpty())  throw new IllegalArgumentException("Check-in date required");
        if (checkoutStr.isEmpty()) throw new IllegalArgumentException("Check-out date required");

        LocalDate checkin, checkout;
        try { checkin  = LocalDate.parse(checkinStr);  }
        catch(Exception e) { throw new IllegalArgumentException("Invalid check-in date format"); }
        try { checkout = LocalDate.parse(checkoutStr); }
        catch(Exception e) { throw new IllegalArgumentException("Invalid check-out date format"); }

        if (checkin.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        if (!checkout.isAfter(checkin))
            throw new IllegalArgumentException("Check-out must be after check-in");
        if (ChronoUnit.DAYS.between(checkin, checkout) > 365)
            throw new IllegalArgumentException("Stay cannot exceed 365 nights");

        // 4. Validate room type
        String roomType = json.optString("roomType", "").trim();
        if (!List.of("Single","Double","Deluxe","Suite").contains(roomType))
            throw new IllegalArgumentException("Invalid room type");

        // 5. Check availability
        List<Room> available = roomDAO.findAvailableRooms(roomType, checkin, checkout);
        if (available.isEmpty())
            throw new IllegalArgumentException("No " + roomType + " rooms available for selected dates");

        // 6. Find/create guest
        Guest guest = guestSvc.findOrCreate(guestName, contact, address, email);

        // 7. Assign first available room
        Room room = available.get(0);

        // 8. Create reservation
        String resNumber = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation res = new Reservation(resNumber, guest.getGuestId(),
                room.getRoomId(), staff.getStaffId(),
                checkin, checkout);
        boolean saved = resDAO.save(res);
        if (!saved) throw new RuntimeException("Failed to save reservation");

        // 9. Update room status
        roomDAO.updateStatus(room.getRoomId(), "OCCUPIED");

        // 10. Notify observers
        NotificationService.getInstance().notifyCreated(
                new ReservationEvent(res, "CREATED"));

        return new JSONObject()
                .put("message",           "Reservation confirmed")
                .put("reservationNumber", resNumber)
                .put("guestName",         guestName)
                .put("roomNumber",        room.getRoomNumber())
                .put("checkinDate",       checkin.toString())
                .put("checkoutDate",      checkout.toString())
                .toString();
    }

    public String updateReservation(String number, JSONObject json) {
        Reservation res = resDAO.findByNumber(number);
        if (res == null) throw new IllegalArgumentException("Reservation not found");

        if (res.getStatus().equals("CHECKED_OUT") || res.getStatus().equals("CANCELLED"))
            throw new IllegalArgumentException("Cannot update a " + res.getStatus() + " reservation");

        // Update dates if provided
        if (json.has("checkinDate")) {
            LocalDate newCheckin = LocalDate.parse(json.getString("checkinDate"));
            if (newCheckin.isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Check-in cannot be in the past");
            res.setCheckinDate(newCheckin);
        }
        if (json.has("checkoutDate")) {
            LocalDate newCheckout = LocalDate.parse(json.getString("checkoutDate"));
            if (!newCheckout.isAfter(res.getCheckinDate()))
                throw new IllegalArgumentException("Check-out must be after check-in");
            res.setCheckoutDate(newCheckout);
        }
        if (json.has("status")) {
            String newStatus = json.getString("status");
            if (!List.of("CONFIRMED","CHECKED_IN","CHECKED_OUT","CANCELLED").contains(newStatus))
                throw new IllegalArgumentException("Invalid status value");
            res.setStatus(newStatus);
        }

        boolean ok = resDAO.update(res);
        if (!ok) throw new RuntimeException("Update failed");
        return new JSONObject().put("message", "Reservation updated").toString();
    }

    public String cancelReservation(String number) {
        Reservation res = resDAO.findByNumber(number);
        if (res == null) throw new IllegalArgumentException("Reservation not found");

        if (res.getStatus().equals("CHECKED_OUT"))
            throw new IllegalArgumentException("Cannot cancel a checked-out reservation");
        if (res.getStatus().equals("CANCELLED"))
            throw new IllegalArgumentException("Reservation is already cancelled");

        resDAO.updateStatus(number, "CANCELLED");
        roomDAO.updateStatus(res.getRoomId(), "AVAILABLE");

        NotificationService.getInstance().notifyCancelled(
                new ReservationEvent(res, "CANCELLED"));

        return new JSONObject().put("message", "Reservation cancelled").toString();
    }

    private JSONObject toJson(Reservation r) {
        return new JSONObject()
                .put("reservationId",     r.getReservationId())
                .put("reservationNumber", r.getReservationNumber())
                .put("guestId",           r.getGuestId())
                .put("roomId",            r.getRoomId())
                .put("staffId",           r.getStaffId())
                .put("checkinDate",       r.getCheckinDate().toString())
                .put("checkoutDate",      r.getCheckoutDate().toString())
                .put("status",            r.getStatus())
                .put("guestName",         r.getGuestName()  != null ? r.getGuestName()  : "")
                .put("roomNumber",        r.getRoomNumber() != null ? r.getRoomNumber() : "")
                .put("staffName",         r.getStaffName()  != null ? r.getStaffName()  : "");
    }
}