package com.hotel;


import com.hotel.handler.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        // Static files
        server.createContext("/", new StaticFileHandler());


        // API endpoints
        server.createContext("/api/auth",        new AuthHandler());
        server.createContext("/api/reservations", new ReservationHandler());
        server.createContext("/api/rooms",        new RoomHandler());
        server.createContext("/api/guests",       new GuestHandler());
        server.createContext("/api/bills",        new BillHandler());
        server.createContext("/api/staff",        new StaffHandler());
        server.createContext("/api/reports",      new ReportHandler());


        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Hotel System running at http://localhost:8080");
    }
}
