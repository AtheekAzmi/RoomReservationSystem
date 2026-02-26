package com.hotel.servlet;

import com.hotel.service.RoomService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/rooms/*")
public class RoomServlet extends BaseServlet {

    private final RoomService service = new RoomService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            String path = req.getPathInfo();

            // GET /api/rooms/available?type=Single&checkin=...&checkout=...
            if ("/available".equals(path)) {
                String type    = req.getParameter("type");
                String checkin = req.getParameter("checkin");
                String checkout= req.getParameter("checkout");
                sendJson(res, 200,
                        service.getAvailableRooms(type, checkin, checkout));
            } else {
                // GET /api/rooms
                sendJson(res, 200, service.getAllRooms());
            }
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
            // PUT /api/rooms/{id}/status
            String path    = req.getPathInfo(); // /{id}/status
            String[] parts = path.split("/");
            int roomId     = Integer.parseInt(parts[1]);
            String body    = readBody(req);
            String status  = new JSONObject(body).getString("roomStatus");

            // All authenticated staff may mark a room AVAILABLE (e.g. after checkout).
            // Only admins may set OCCUPIED or MAINTENANCE.
            if (!"AVAILABLE".equals(status) && !isAdmin(req)) {
                sendError(res, 403, "Admin only"); return;
            }
            sendJson(res, 200, service.updateRoomStatus(roomId, status));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}