package com.hotel.observer;

import com.hotel.dao.RoomDAO;

// Observer 2: Automatically manages room status when reservations change
public class RoomStatusObserver implements ReservationObserver {

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    public void onReservationCreated(ReservationEvent event) {
        // Room is already set to OCCUPIED in ReservationService
        // This observer just logs confirmation
        int roomId = event.getReservation().getRoomId();
        System.out.println("[RoomStatusObserver] Room " + roomId +
                " marked OCCUPIED for reservation " +
                event.getReservation().getReservationNumber());
    }

    @Override
    public void onReservationCancelled(ReservationEvent event) {
        // When cancelled → free up the room
        int roomId = event.getReservation().getRoomId();
        boolean ok = roomDAO.updateStatus(roomId, "AVAILABLE");
        System.out.println("[RoomStatusObserver] Room " + roomId +
                (ok ? " marked AVAILABLE" : " status update FAILED") +
                " after cancellation of " +
                event.getReservation().getReservationNumber());
    }

    @Override
    public void onReservationUpdated(ReservationEvent event) {
        String status = event.getReservation().getStatus();
        int roomId    = event.getReservation().getRoomId();

        switch (status) {
            case "CHECKED_IN":
                roomDAO.updateStatus(roomId, "OCCUPIED");
                break;
            case "CHECKED_OUT":
                roomDAO.updateStatus(roomId, "AVAILABLE");
                break;
            case "CANCELLED":
                roomDAO.updateStatus(roomId, "AVAILABLE");
                break;
            default:
                // CONFIRMED → no room status change needed
                break;
        }

        System.out.println("[RoomStatusObserver] Room " + roomId +
                " status synced for event: " + status);
    }
}
//```
//
//        ---
//
//        ## ✅ Complete File List — Everything You Need
//```
//        com/hotel/
//        │
//        ├── config/
//        │   └── DatabaseConfig.java          ✅ (from guide doc)
//        │
//        ├── model/
//        │   ├── Staff.java                   ✅
//        │   ├── Guest.java                   ✅
//        │   ├── RoomType.java                ✅
//        │   ├── RoomRate.java                ✅
//        │   ├── Room.java                    ✅
//        │   ├── Reservation.java             ✅
//        │   ├── Bill.java                    ✅
//        │   └── Payment.java                 ✅
//        │
//        ├── dao/
//        │   ├── StaffDAO.java                ✅
//        │   ├── GuestDAO.java                ✅
//        │   ├── RoomDAO.java                 ✅
//        │   ├── RoomRateDAO.java             ✅
//        │   ├── ReservationDAO.java          ✅
//        │   ├── BillDAO.java                 ✅
//        │   └── PaymentDAO.java              ✅
//        │
//        ├── service/
//        │   ├── AuthService.java             ✅
//        │   ├── GuestService.java            ✅
//        │   ├── RoomService.java             ✅
//        │   ├── ReservationService.java      ✅
//        │   ├── BillingService.java          ✅
//        │   ├── StaffService.java            ✅
//        │   └── ReportService.java           ✅
//        │
//        ├── util/
//        │   ├── PasswordUtil.java            ✅ ← NEW
//│   ├── SessionManager.java          ✅ ← NEW
//│   ├── JsonUtil.java                ✅ ← NEW
//│   └── ValidationUtil.java          ✅ ← NEW
//│
//        ├── observer/
//        │   ├── ReservationEvent.java        ✅ ← NEW
//│   ├── ReservationObserver.java     ✅ ← NEW
//│   ├── NotificationService.java     ✅ ← NEW
//│   ├── AuditLogObserver.java        ✅ ← NEW
//│   └── RoomStatusObserver.java      ✅ ← NEW
//│
//        └── Main.java                        ✅ (from earlier)