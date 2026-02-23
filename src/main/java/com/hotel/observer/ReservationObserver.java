package com.hotel.observer;

// Interface that all observers must implement
public interface ReservationObserver {

    void onReservationCreated(ReservationEvent event);

    void onReservationCancelled(ReservationEvent event);

    void onReservationUpdated(ReservationEvent event);
}