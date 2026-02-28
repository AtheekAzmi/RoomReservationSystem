package com.roomreservation.model;

import java.util.Objects;

public class Guest {

    private final String guestId;
    private final String guestName;
    private final String address;
    private final String contactNumber;

    public Guest(String guestId, String guestName, String address, String contactNumber) {
        if (guestId == null || guestId.isEmpty())
            throw new IllegalArgumentException("Guest ID cannot be null or empty");
        if (guestName == null || guestName.isBlank())
            throw new IllegalArgumentException("Guest name cannot be null or blank");
        if (contactNumber == null || !contactNumber.matches("\\d{10}"))
            throw new IllegalArgumentException("Contact number must be exactly 10 digits");
        this.guestId       = guestId;
        this.guestName     = guestName;
        this.address       = address;
        this.contactNumber = contactNumber;
    }

    public String getGuestId()       { return guestId; }
    public String getGuestName()     { return guestName; }
    public String getAddress()       { return address; }
    public String getContactNumber() { return contactNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest)) return false;
        return guestId.equals(((Guest) o).guestId);
    }

    @Override
    public int hashCode() { return Objects.hash(guestId); }

    @Override
    public String toString() {
        return "Guest{guestId='" + guestId + "', name='" + guestName
                + "', contact='" + contactNumber + "'}";
    }
}
