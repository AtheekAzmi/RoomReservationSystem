package com.roomreservation.model;

public class Room {

    private final String roomId;
    private final String roomNumber;
    private final RoomType roomType;
    private RoomStatus status;
    private final int floorNumber;

    public Room(String roomId, String roomNumber, RoomType roomType,
                RoomStatus status, int floorNumber) {
        if (roomId == null)
            throw new IllegalArgumentException("Room ID cannot be null");
        if (floorNumber < 0)
            throw new IllegalArgumentException("Floor number cannot be negative");
        this.roomId      = roomId;
        this.roomNumber  = roomNumber;
        this.roomType    = roomType;
        this.status      = status;
        this.floorNumber = floorNumber;
    }

    public String     getRoomId()     { return roomId; }
    public String     getRoomNumber() { return roomNumber; }
    public RoomType   getRoomType()   { return roomType; }
    public RoomStatus getStatus()     { return status; }
    public int        getFloorNumber(){ return floorNumber; }

    public void setStatus(RoomStatus status) { this.status = status; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }
}
