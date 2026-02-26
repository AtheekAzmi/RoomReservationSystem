package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int           paymentId;
    private int           billId;
    private BigDecimal    amountPaid;
    private BigDecimal    discountAmount;
    private String        paymentMethod; // CASH, CARD, ONLINE
    private String        paymentStatus; // SUCCESS, FAILED, PENDING
    private LocalDateTime paymentDate;

    public Payment() {}

    public Payment(int billId, BigDecimal amountPaid,
                   String paymentMethod, BigDecimal discountAmount) {
        this.billId         = billId;
        this.amountPaid     = amountPaid;
        this.paymentMethod  = paymentMethod;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.paymentStatus  = "SUCCESS";
    }

    public int           getPaymentId()          { return paymentId; }
    public void          setPaymentId(int id)    { this.paymentId = id; }

    public int           getBillId()          { return billId; }
    public void          setBillId(int id)    { this.billId = id; }

    public BigDecimal    getAmountPaid()              { return amountPaid; }
    public void          setAmountPaid(BigDecimal a)  { this.amountPaid = a; }

    public BigDecimal    getDiscountAmount()              { return discountAmount; }
    public void          setDiscountAmount(BigDecimal d)  { this.discountAmount = d != null ? d : BigDecimal.ZERO; }

    public String        getPaymentMethod()          { return paymentMethod; }
    public void          setPaymentMethod(String m)  { this.paymentMethod = m; }

    public String        getPaymentStatus()          { return paymentStatus; }
    public void          setPaymentStatus(String s)  { this.paymentStatus = s; }

    public LocalDateTime getPaymentDate()              { return paymentDate; }
    public void          setPaymentDate(LocalDateTime d){ this.paymentDate = d; }
}