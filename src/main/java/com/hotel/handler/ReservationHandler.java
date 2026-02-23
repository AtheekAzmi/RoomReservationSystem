package com.hotel.handler;

import com.hotel.service.ReservationService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;

public class ReservationHandler extends BaseHandler implements HttpHandler {

    private final ReservationService service = new ReservationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            handleCors(ex); return;
        }
        if (!isAuthenticated(ex)) {
            sendError(ex, 401, "Unauthorized"); return;
        }

        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        // e.g. /api/reservations  or  /api/reservations/RES-XXXXXXXX

        try {
            switch (method) {
                case "GET" -> {
                    // GET /api/reservations  → all reservations
                    if (path.equals("/api/reservations") ||
                            path.equals("/api/reservations/")) {
                        sendJson(ex, 200, service.getAllReservations());
                    } else {
                        // GET /api/reservations/{number}
                        String number = extractLastSegment(path);
                        sendJson(ex, 200, service.getReservation(number));
                    }
                }
                case "POST" -> {
                    // POST /api/reservations  → create
                    String body   = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    String result = service.createReservation(json, getToken(ex));
                    sendJson(ex, 201, result);
                }
                case "PUT" -> {
                    // PUT /api/reservations/{number}  → update
                    String number   = extractLastSegment(path);
                    String body     = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    sendJson(ex, 200, service.updateReservation(number, json));
                }
                case "DELETE" -> {
                    // DELETE /api/reservations/{number}  → cancel
                    String number = extractLastSegment(path);
                    sendJson(ex, 200, service.cancelReservation(number));
                }
                default -> sendError(ex, 405, "Method not allowed");
            }

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Internal server error");
        }
    }

    private String extractLastSegment(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}