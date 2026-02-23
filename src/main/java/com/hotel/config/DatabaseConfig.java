package com.hotel.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConfig {
    private static DatabaseConfig instance;
    private static final String URL =
            "jdbc:mysql://localhost:3306/hotel_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "pass123";


    private DatabaseConfig() {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }


    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) instance = new DatabaseConfig();
        return instance;
    }


    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
