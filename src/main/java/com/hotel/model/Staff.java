package com.hotel.model;

public class Staff {
    private int staffId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role; // admin, receptionist

    public Staff() {}

    public Staff(String username, String passwordHash,
                 String fullName, String email, String role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.role         = role;
    }

    public int    getStaffId()      { return staffId; }
    public void   setStaffId(int id){ this.staffId = id; }

    public String getUsername()          { return username; }
    public void   setUsername(String u)  { this.username = u; }

    public String getPasswordHash()          { return passwordHash; }
    public void   setPasswordHash(String p)  { this.passwordHash = p; }

    public String getFullName()          { return fullName; }
    public void   setFullName(String n)  { this.fullName = n; }

    public String getEmail()         { return email; }
    public void   setEmail(String e) { this.email = e; }

    public String getRole()          { return role; }
    public void   setRole(String r)  { this.role = r; }
}