package com.hotel.servlet;

import com.hotel.service.ReservationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/reservations/*")
public class ReservationServlet extends BaseServlet {

    private final ReservationService service = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {

        System.out.println("[ReservationServlet] GET request");
        System.out.println("[ReservationServlet] Auth header: " +
                req.getHeader("Authorization"));

        if (!isAuthenticated(req)) {
            System.out.println("[ReservationServlet] NOT authenticated — returning 401");
            sendError(res, 401, "Unauthorized");
            return;
        }

        System.out.println("[ReservationServlet] Authenticated OK");

        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                sendJson(res, 200, service.getAllReservations());
            } else {
                String number = path.substring(1);
                sendJson(res, 200, service.getReservation(number));
            }
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(res, 500, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 201, service.createReservation(json, getToken(req)));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            String path   = req.getPathInfo();
            String number = path.substring(1);
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 200, service.updateReservation(number, json));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            String path   = req.getPathInfo();
            String number = path.substring(1);
            sendJson(res, 200, service.cancelReservation(number));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}