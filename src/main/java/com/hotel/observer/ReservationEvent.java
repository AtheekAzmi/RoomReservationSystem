package com.hotel.observer;

import com.hotel.model.Reservation;
import java.time.LocalDateTime;

public class ReservationEvent {

    // Event types
    public static final String CREATED   = "CREATED";
    public static final String CANCELLED = "CANCELLED";
    public static final String UPDATED   = "UPDATED";
    public static final String CHECKED_IN  = "CHECKED_IN";
    public static final String CHECKED_OUT = "CHECKED_OUT";

    private final Reservation     reservation;
    private final String          eventType;
    private final LocalDateTime   occurredAt;

    public ReservationEvent(Reservation reservation, String eventType) {
        this.reservation = reservation;
        this.eventType   = eventType;
        this.occurredAt  = LocalDateTime.now();
    }

    public Reservation   getReservation() { return reservation; }
    public String        getEventType()   { return eventType;   }
    public LocalDateTime getOccurredAt()  { return occurredAt;  }

    @Override
    public String toString() {
        return "[ReservationEvent] Type=" + eventType +
                " | Reservation=" + reservation.getReservationNumber() +
                " | At=" + occurredAt;
    }
}