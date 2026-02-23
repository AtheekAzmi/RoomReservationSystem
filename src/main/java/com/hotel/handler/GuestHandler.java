package com.hotel.handler;

import com.hotel.service.GuestService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;

public class GuestHandler extends BaseHandler implements HttpHandler {

    private final GuestService service = new GuestService();

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

        try {
            switch (method) {
                case "GET" -> {
                    // GET /api/guests  → all guests
                    if (path.equals("/api/guests") ||
                            path.equals("/api/guests/")) {
                        sendJson(ex, 200, service.getAllGuests());
                    } else {
                        // GET /api/guests/{id}
                        int id = Integer.parseInt(extractLastSegment(path));
                        sendJson(ex, 200, service.getGuest(id));
                    }
                }
                case "POST" -> {
                    // POST /api/guests  → create guest
                    String body     = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    sendJson(ex, 201, service.createGuest(json));
                }
                case "PUT" -> {
                    // PUT /api/guests/{id}  → update guest
                    int id          = Integer.parseInt(extractLastSegment(path));
                    String body     = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    sendJson(ex, 200, service.updateGuest(id, json));
                }
                default -> sendError(ex, 405, "Method not allowed");
            }

        } catch (NumberFormatException e) {
            sendError(ex, 400, "Invalid guest ID");
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