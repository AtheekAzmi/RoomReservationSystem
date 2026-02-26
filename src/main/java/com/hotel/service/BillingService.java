package com.hotel.service;

import com.hotel.dao.BillDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BillingService {

    private final BillDAO        billDAO    = new BillDAO();
    private final PaymentDAO     paymentDAO = new PaymentDAO();
    private final ReservationDAO resDAO     = new ReservationDAO();
    private final RoomService    roomSvc    = new RoomService();

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public String generateBill(String reservationNumber) {
        Reservation res = resDAO.findByNumber(reservationNumber);
        if (res == null)
            throw new IllegalArgumentException("Reservation not found: " + reservationNumber);

        if (res.getStatus().equals("CANCELLED"))
            throw new IllegalArgumentException("Cannot bill a cancelled reservation");

        // Check if bill already exists
        Bill existing = billDAO.findByReservationId(res.getReservationId());
        if (existing != null) return toBillJson(existing).toString();

        // Calculate nights
        long nights = ChronoUnit.DAYS.between(res.getCheckinDate(), res.getCheckoutDate());
        if (nights <= 0)
            throw new IllegalArgumentException("Invalid stay duration");

        // Get current room rate
        RoomRate rate = roomSvc.getCurrentRate(res.getRoomId());
        BigDecimal ratePerNight = rate.getRatePerNight();

        // Calculations
        BigDecimal subtotal  = ratePerNight.multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total     = subtotal.add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill();
        bill.setBillNumber   ("BILL-" + System.currentTimeMillis());
        bill.setReservationId(res.getReservationId());
        bill.setSubtotal     (subtotal);
        bill.setTaxRate      (TAX_RATE.multiply(new BigDecimal("100")));
        bill.setTaxAmount    (taxAmount);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTotalAmount  (total);
        bill.setPaymentStatus("PENDING");

        boolean saved = billDAO.save(bill);
        if (!saved) throw new RuntimeException("Failed to generate bill");

        // Mark reservation as checked out
        resDAO.updateStatus(reservationNumber, "CHECKED_OUT");

        return toBillJson(bill).toString();
    }

    public String getBill(int billId) {
        Bill b = billDAO.findById(billId);
        if (b == null) throw new IllegalArgumentException("Bill not found");
        return toBillJson(b).toString();
    }

    public String getAllBills() {
        List<Bill> list = billDAO.findAll();
        JSONArray arr = new JSONArray();
        for (Bill b : list) arr.put(toBillJson(b));
        return arr.toString();
    }

    public String adjustBill(int billId, JSONObject json) {
        Bill b = billDAO.findById(billId);
        if (b == null) throw new IllegalArgumentException("Bill not found");
        if ("PAID".equals(b.getPaymentStatus()))
            throw new IllegalArgumentException("Cannot adjust a fully paid bill");

        BigDecimal discountAmount = new BigDecimal(json.optString("discountAmount", "0"))
                .setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        BigDecimal taxRate        = new BigDecimal(json.optString("taxRate", "10"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal additionalTax  = new BigDecimal(json.optString("additionalTax", "0"))
                .setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        // Recalculate from DB subtotal (source of truth — never trust client subtotal)
        BigDecimal subtotal   = b.getSubtotal();
        BigDecimal afterDisc  = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        BigDecimal taxAmount  = afterDisc
                .multiply(taxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = afterDisc.add(taxAmount).add(additionalTax)
                .setScale(2, RoundingMode.HALF_UP);

        billDAO.updateAdjustments(billId, discountAmount, taxRate, taxAmount, totalAmount);

        b.setDiscountAmount(discountAmount);
        b.setTaxRate(taxRate);
        b.setTaxAmount(taxAmount);
        b.setTotalAmount(totalAmount);
        return toBillJson(b).toString();
    }

    public String processPayment(JSONObject json) {
        int    billId  = json.optInt   ("billId", -1);
        String method  = json.optString("paymentMethod", "").trim();
        String amtStr  = json.optString("amount", "0");

        if (billId == -1)
            throw new IllegalArgumentException("Bill ID is required");
        if (!List.of("CASH","CARD","ONLINE").contains(method))
            throw new IllegalArgumentException("Invalid payment method (CASH, CARD, ONLINE)");

        BigDecimal amount;
        try { amount = new BigDecimal(amtStr).setScale(2, RoundingMode.HALF_UP); }
        catch(Exception e) { throw new IllegalArgumentException("Invalid payment amount"); }

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Payment amount must be greater than 0");

        Bill b = billDAO.findById(billId);
        if (b == null) throw new IllegalArgumentException("Bill not found");
        if (b.getPaymentStatus().equals("PAID"))
            throw new IllegalArgumentException("Bill is already fully paid");

        if (amount.compareTo(b.getTotalAmount()) > 0)
            throw new IllegalArgumentException("Payment exceeds bill total");

        Payment p = new Payment(billId, amount, method, b.getDiscountAmount());
        paymentDAO.save(p);

        // Update bill status
        String newStatus = amount.compareTo(b.getTotalAmount()) >= 0 ? "PAID" : "PARTIAL";
        billDAO.updatePaymentStatus(billId, newStatus);

        // When fully paid, automatically release the room back to AVAILABLE
        if ("PAID".equals(newStatus)) {
            Reservation reservation = resDAO.findById(b.getReservationId());
            if (reservation != null) {
                roomSvc.updateRoomStatus(reservation.getRoomId(), "AVAILABLE");
            }
        }

        return new JSONObject()
                .put("message",       "Payment processed")
                .put("paymentStatus", newStatus)
                .put("amountPaid",    amount)
                .toString();
    }

    private JSONObject toBillJson(Bill b) {
        return new JSONObject()
                .put("billId",            b.getBillId())
                .put("billNumber",        b.getBillNumber())
                .put("reservationId",     b.getReservationId())
                .put("subtotal",          b.getSubtotal())
                .put("taxRate",           b.getTaxRate())
                .put("taxAmount",         b.getTaxAmount())
                .put("discountAmount",    b.getDiscountAmount())
                .put("totalAmount",       b.getTotalAmount())
                .put("paymentStatus",     b.getPaymentStatus())
                .put("reservationNumber", b.getReservationNumber() != null ? b.getReservationNumber() : "")
                .put("guestName",         b.getGuestName()         != null ? b.getGuestName()         : "");
    }
}