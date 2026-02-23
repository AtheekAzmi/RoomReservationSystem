package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RoomRate {
    private int        roomRateId;
    private int        roomTypeId;
    private BigDecimal ratePerNight;
    private int        maxOccupancy;
    private String     description;
    private LocalDate  effectiveFrom;
    private LocalDate  effectiveTo;

    public RoomRate() {}

    public int        getRoomRateId()           { return roomRateId; }
    public void       setRoomRateId(int id)     { this.roomRateId = id; }

    public int        getRoomTypeId()           { return roomTypeId; }
    public void       setRoomTypeId(int id)     { this.roomTypeId = id; }

    public BigDecimal getRatePerNight()              { return ratePerNight; }
    public void       setRatePerNight(BigDecimal r)  { this.ratePerNight = r; }

    public int        getMaxOccupancy()          { return maxOccupancy; }
    public void       setMaxOccupancy(int m)     { this.maxOccupancy = m; }

    public String     getDescription()           { return description; }
    public void       setDescription(String d)   { this.description = d; }

    public LocalDate  getEffectiveFrom()             { return effectiveFrom; }
    public void       setEffectiveFrom(LocalDate d)  { this.effectiveFrom = d; }

    public LocalDate  getEffectiveTo()             { return effectiveTo; }
    public void       setEffectiveTo(LocalDate d)  { this.effectiveTo = d; }
}