package com.roomreservation.model;

import java.util.UUID;

public class Bill {

    private final String      billId;
    private final String      billNumber;
    private final Reservation reservation;
    private final double      ratePerNight;
    private final double      taxRate;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private String paymentStatus;

    public Bill(Reservation reservation, double ratePerNight) {
        this(reservation, ratePerNight, 0.10);
    }

    public Bill(Reservation reservation, double ratePerNight, double taxRate) {
        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");
        if (ratePerNight <= 0)
            throw new IllegalArgumentException("Rate per night must be positive");
        this.billId       = UUID.randomUUID().toString();
        this.billNumber   = "BILL-" + billId.substring(0, 8).toUpperCase();
        this.reservation  = reservation;
        this.ratePerNight = ratePerNight;
        this.taxRate      = taxRate;
        this.paymentStatus = "UNPAID";
    }

    public void calculateTotal() {
        this.subtotal    = reservation.calculateNights() * ratePerNight;
        this.taxAmount   = subtotal * taxRate;
        this.totalAmount = subtotal + taxAmount;
    }

    public String      getBillId()        { return billId; }
    public String      getBillNumber()    { return billNumber; }
    public double      getSubtotal()      { return subtotal; }
    public double      getTaxAmount()     { return taxAmount; }
    public double      getTotalAmount()   { return totalAmount; }
    public String      getPaymentStatus() { return paymentStatus; }
    public Reservation getReservation()  { return reservation; }
}
