package com.roomreservation.store;

import com.roomreservation.model.Guest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GuestStore {

    private final Map<String, Guest> byContact = new LinkedHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public Guest findByContact(String contactNumber) {
        return byContact.get(contactNumber);
    }

    public Guest createGuest(String name, String address, String contactNumber) {
        String id = "G" + String.format("%03d", idCounter.getAndIncrement());
        Guest guest = new Guest(id, name, address, contactNumber);
        byContact.put(contactNumber, guest);
        return guest;
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(byContact.values());
    }
}
