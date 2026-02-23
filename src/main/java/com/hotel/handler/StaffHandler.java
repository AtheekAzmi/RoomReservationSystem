package com.hotel.handler;

import com.hotel.service.StaffService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;

public class StaffHandler extends BaseHandler implements HttpHandler {

    private final StaffService service = new StaffService();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            handleCors(ex); return;
        }
        if (!isAuthenticated(ex)) {
            sendError(ex, 401, "Unauthorized"); return;
        }
        // All staff operations require admin role
        if (!isAdmin(ex)) {
            sendError(ex, 403, "Admin access required"); return;
        }

        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET" -> {
                    // GET /api/staff  → all staff
                    sendJson(ex, 200, service.getAllStaff());
                }
                case "POST" -> {
                    // POST /api/staff  → create staff
                    String body     = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    sendJson(ex, 201, service.createStaff(json));
                }
                case "PUT" -> {
                    // PUT /api/staff/{id}  → update staff
                    int id          = Integer.parseInt(extractLastSegment(path));
                    String body     = readBody(ex);
                    JSONObject json = new JSONObject(body);
                    sendJson(ex, 200, service.updateStaff(id, json));
                }
                case "DELETE" -> {
                    // DELETE /api/staff/{id}  → delete staff
                    int id = Integer.parseInt(extractLastSegment(path));
                    sendJson(ex, 200, service.deleteStaff(id));
                }
                default -> sendError(ex, 405, "Method not allowed");
            }

        } catch (NumberFormatException e) {
            sendError(ex, 400, "Invalid staff ID");
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