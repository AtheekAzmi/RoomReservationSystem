package com.hotel;

import com.hotel.util.PasswordUtil;
import com.hotel.dao.StaffDAO;
import com.hotel.model.Staff;

public class TestHash {
    public static void main(String[] args) {

        // Print the correct hash for Admin@123
        String hash = PasswordUtil.hash("Admin@123");
        System.out.println("=== CORRECT HASH FOR Admin@123 ===");
        System.out.println(hash);
        System.out.println("Hash length: " + hash.length());

        // Check what's currently stored in DB
        System.out.println("\n=== WHAT'S STORED IN DATABASE ===");
        StaffDAO dao = new StaffDAO();
        Staff admin = dao.findByUsername("admin");

        if (admin == null) {
            System.out.println("❌ Admin user NOT FOUND in database!");
            System.out.println("   You need to INSERT the admin user.");
        } else {
            System.out.println("✅ Admin found: " + admin.getFullName());
            System.out.println("   Stored hash:  " + admin.getPasswordHash());
            System.out.println("   Correct hash: " + hash);
            System.out.println("   Match: " + hash.equals(admin.getPasswordHash()));
        }

        // Print the SQL you need to run
        System.out.println("\n=== RUN THIS SQL IN MYSQL ===");
        System.out.println("DELETE FROM staff WHERE username = 'admin';");
        System.out.println("INSERT INTO staff " +
                "(username, password_hash, full_name, email, role) VALUES (");
        System.out.println("  'admin',");
        System.out.println("  '" + hash + "',");
        System.out.println("  'System Administrator',");
        System.out.println("  'admin@hotel.com',");
        System.out.println("  'admin'");
        System.out.println(");");
    }
}