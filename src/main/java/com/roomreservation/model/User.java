package com.roomreservation.model;

public class User {

    private final String username;
    private final String hashedPassword;
    private final String role;

    public User(String username, String hashedPassword, String role) {
        this.username       = username;
        this.hashedPassword = hashedPassword;
        this.role           = role;
    }

    public String getUsername()       { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getRole()           { return role; }
}
