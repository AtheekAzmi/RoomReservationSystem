package com.hotel.handler;

import com.hotel.service.RoomService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RoomHandler extends BaseHandler implements HttpHandler {

    private final RoomService service = new RoomService();

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
            // GET /api/rooms/available?type=Single&checkin=...&checkout=...
            if (method.equals("GET") && path.contains("/available")) {
                Map<String, String> params = parseQuery(
                        ex.getRequestURI().getQuery());
                String type    = params.getOrDefault("type",     "");
                String checkin = params.getOrDefault("checkin",  "");
                String checkout= params.getOrDefault("checkout", "");
                sendJson(ex, 200,
                        service.getAvailableRooms(type, checkin, checkout));

                // GET /api/rooms  → all rooms
            } else if (method.equals("GET")) {
                sendJson(ex, 200, service.getAllRooms());

                // PUT /api/rooms/{id}/status  → update status (admin only)
            } else if (method.equals("PUT")) {
                if (!isAdmin(ex)) {
                    sendError(ex, 403, "Admin access required"); return;
                }
                String[] parts = path.split("/");
                int roomId     = Integer.parseInt(parts[parts.length - 2]);
                String body    = readBody(ex);
                String status  = new JSONObject(body).getString("roomStatus");
                sendJson(ex, 200, service.updateRoomStatus(roomId, status));

            } else {
                sendError(ex, 405, "Method not allowed");
            }

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Internal server error");
        }
    }

    // Parse query string  → Map
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2)
                map.put(kv[0], kv[1]);
        }
        return map;
    }
}