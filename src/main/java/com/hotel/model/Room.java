package com.hotel.model;

public class Room {
    private int    roomId;
    private String roomNumber;
    private int    floorNumber;
    private int    roomTypeId;
    private String roomStatus;   // AVAILABLE, OCCUPIED, MAINTENANCE
    private String typeName;     // joined field for display

    public Room() {}

    public Room(String roomNumber, int floorNumber,
                int roomTypeId, String roomStatus) {
        this.roomNumber  = roomNumber;
        this.floorNumber = floorNumber;
        this.roomTypeId  = roomTypeId;
        this.roomStatus  = roomStatus;
    }

    public int    getRoomId()        { return roomId; }
    public void   setRoomId(int id)  { this.roomId = id; }

    public String getRoomNumber()          { return roomNumber; }
    public void   setRoomNumber(String n)  { this.roomNumber = n; }

    public int    getFloorNumber()          { return floorNumber; }
    public void   setFloorNumber(int f)     { this.floorNumber = f; }

    public int    getRoomTypeId()          { return roomTypeId; }
    public void   setRoomTypeId(int id)    { this.roomTypeId = id; }

    public String getRoomStatus()          { return roomStatus; }
    public void   setRoomStatus(String s)  { this.roomStatus = s; }

    public String getTypeName()          { return typeName; }
    public void   setTypeName(String t)  { this.typeName = t; }
}