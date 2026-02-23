package com.hotel.model;

public class RoomType {
    private int    roomTypeId;
    private String typeName;      // Single, Double, Deluxe, Suite
    private String description;
    private int    maxOccupancy;

    public RoomType() {}

    public RoomType(String typeName, String description, int maxOccupancy) {
        this.typeName     = typeName;
        this.description  = description;
        this.maxOccupancy = maxOccupancy;
    }

    public int    getRoomTypeId()        { return roomTypeId; }
    public void   setRoomTypeId(int id)  { this.roomTypeId = id; }

    public String getTypeName()          { return typeName; }
    public void   setTypeName(String t)  { this.typeName = t; }

    public String getDescription()          { return description; }
    public void   setDescription(String d)  { this.description = d; }

    public int    getMaxOccupancy()          { return maxOccupancy; }
    public void   setMaxOccupancy(int m)     { this.maxOccupancy = m; }
}