package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill {
    private int          billId;
    private String       billNumber;
    private int          reservationId;
    private BigDecimal   subtotal;
    private BigDecimal   taxRate;
    private BigDecimal   taxAmount;
    private BigDecimal   discountAmount;
    private BigDecimal   totalAmount;
    private String       paymentStatus; // PENDING, PAID, PARTIAL
    private LocalDateTime generatedAt;

    // Joined display fields
    private String reservationNumber;
    private String guestName;

    public Bill() {
        this.discountAmount = BigDecimal.ZERO;
        this.paymentStatus  = "PENDING";
    }

    public int         getBillId()          { return billId; }
    public void        setBillId(int id)    { this.billId = id; }

    public String      getBillNumber()          { return billNumber; }
    public void        setBillNumber(String n)  { this.billNumber = n; }

    public int         getReservationId()          { return reservationId; }
    public void        setReservationId(int id)    { this.reservationId = id; }

    public BigDecimal  getSubtotal()              { return subtotal; }
    public void        setSubtotal(BigDecimal s)  { this.subtotal = s; }

    public BigDecimal  getTaxRate()               { return taxRate; }
    public void        setTaxRate(BigDecimal t)   { this.taxRate = t; }

    public BigDecimal  getTaxAmount()             { return taxAmount; }
    public void        setTaxAmount(BigDecimal t) { this.taxAmount = t; }

    public BigDecimal  getDiscountAmount()              { return discountAmount; }
    public void        setDiscountAmount(BigDecimal d)  { this.discountAmount = d; }

    public BigDecimal  getTotalAmount()              { return totalAmount; }
    public void        setTotalAmount(BigDecimal t)  { this.totalAmount = t; }

    public String      getPaymentStatus()          { return paymentStatus; }
    public void        setPaymentStatus(String s)  { this.paymentStatus = s; }

    public LocalDateTime getGeneratedAt()              { return generatedAt; }
    public void          setGeneratedAt(LocalDateTime d){ this.generatedAt = d; }

    public String      getReservationNumber()          { return reservationNumber; }
    public void        setReservationNumber(String n)  { this.reservationNumber = n; }

    public String      getGuestName()          { return guestName; }
    public void        setGuestName(String n)  { this.guestName = n; }
}