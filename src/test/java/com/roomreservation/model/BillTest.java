package com.roomreservation.model;

import com.roomreservation.model.Bill;
import com.roomreservation.model.Guest;
import com.roomreservation.model.Reservation;
import com.roomreservation.model.Room;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test Suite for Bill model class.
 *
 * Requirements covered:
 *  - UC-04: Calculate and Print Bill
 *  - Sequence Diagram: Bill: subtotal = nights × ratePerNight,
 *                            totalAmount = subtotal + taxAmount (10%)
 */
@DisplayName("Bill Model Tests")
public class BillTest {

    private static final double RATE_SINGLE  = 5000.0;   // LKR per night
    private static final double RATE_DOUBLE  = 8000.0;
    private static final double RATE_DELUXE  = 12000.0;
    private static final double RATE_SUITE   = 20000.0;
    private static final double TAX_RATE     = 0.10;     // 10%
    private static final double DELTA        = 0.001;

    private Guest guest;
    private Room room;
    private Reservation reservation;
    private Bill bill;

    @BeforeEach
    void setUp() {
        guest       = new Guest("G001", "Ravi Perera", "10 Temple Rd", "0762345678");
        room        = new Room("R301", "301", RoomType.SINGLE, RoomStatus.AVAILABLE, 3);
        reservation = new Reservation("RES-B01", guest, room,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 14));   // 4 nights
        bill        = new Bill(reservation, RATE_SINGLE);
    }

    // ── TC-BILL-01 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-01: Bill generated with non-null ID and number")
    void testBillCreation() {
        assertNotNull(bill);
        assertNotNull(bill.getBillId());
        assertNotNull(bill.getBillNumber());
    }

    // ── TC-BILL-02 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-02: Subtotal = nights × ratePerNight (4 nights × 5000 = 20000)")
    void testSubtotalCalculation() {
        bill.calculateTotal();
        assertEquals(20000.0, bill.getSubtotal(), DELTA,
                "Subtotal should be 4 × 5000 = 20000");
    }

    // ── TC-BILL-03 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-03: Tax amount = subtotal × 10%")
    void testTaxCalculation() {
        bill.calculateTotal();
        assertEquals(2000.0, bill.getTaxAmount(), DELTA,
                "Tax should be 10% of 20000 = 2000");
    }

    // ── TC-BILL-04 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-04: Total amount = subtotal + tax")
    void testTotalAmountCalculation() {
        bill.calculateTotal();
        assertEquals(22000.0, bill.getTotalAmount(), DELTA,
                "Total should be 20000 + 2000 = 22000");
    }

    // ── TC-BILL-05 ──────────────────────────────────────────────
    @ParameterizedTest(name = "TC-BILL-05 [{index}]: {0} nights @ {1} LKR/night → total {4}")
    @CsvSource({
            "1, 5000.0,  5000.0,   500.0,  5500.0,  SINGLE",
            "3, 8000.0, 24000.0,  2400.0, 26400.0,  DOUBLE",
            "7, 12000.0, 84000.0, 8400.0, 92400.0,  DELUXE",
            "2, 20000.0, 40000.0, 4000.0, 44000.0,  SUITE"
    })
    @DisplayName("TC-BILL-05: Parametrised billing across room types")
    void testBillingAcrossRoomTypes(int nights, double rate,
                                    double expectedSubtotal, double expectedTax,
                                    double expectedTotal, String type) {
        LocalDate ci = LocalDate.of(2026, 5, 1);
        LocalDate co = ci.plusDays(nights);
        Room r = new Room("RX", "X", RoomType.valueOf(type), RoomStatus.AVAILABLE, 1);
        Reservation res = new Reservation(null, guest, r, ci, co);
        Bill b = new Bill(res, rate);
        b.calculateTotal();

        assertEquals(expectedSubtotal, b.getSubtotal(),   DELTA);
        assertEquals(expectedTax,      b.getTaxAmount(),  DELTA);
        assertEquals(expectedTotal,    b.getTotalAmount(), DELTA);
    }

    // ── TC-BILL-06 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-06: Zero or negative rate throws IllegalArgumentException")
    void testZeroRateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bill(reservation, 0.0),
                "Zero rate should throw");
        assertThrows(IllegalArgumentException.class,
                () -> new Bill(reservation, -500.0),
                "Negative rate should throw");
    }

    // ── TC-BILL-07 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-07: Null reservation throws IllegalArgumentException")
    void testNullReservationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bill(null, RATE_SINGLE));
    }

    // ── TC-BILL-08 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-08: Payment status starts as UNPAID")
    void testInitialPaymentStatus() {
        assertEquals("UNPAID", bill.getPaymentStatus(),
                "New bill should have UNPAID status");
    }

    // ── TC-BILL-09 ──────────────────────────────────────────────
    @Test
    @DisplayName("TC-BILL-09: applyTax with custom rate calculates correctly")
    void testCustomTaxRate() {
        Bill customBill = new Bill(reservation, RATE_DOUBLE, 0.15); // 15% tax
        customBill.calculateTotal();
        double sub = 4 * RATE_DOUBLE; // 32000
        assertEquals(sub * 0.15, customBill.getTaxAmount(), DELTA);
    }
}