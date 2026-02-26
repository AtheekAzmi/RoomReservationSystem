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
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        if (!isAdmin(req))         { sendError(res,403,"Admin only");    return; }
        try {
            String body = readBody(req);
            sendJson(res, 201, service.createRoom(new JSONObject(body)));
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
            String path    = req.getPathInfo(); // /{id} or /{id}/status
            String[] parts = path.split("/");
            int roomId     = Integer.parseInt(parts[1]);
            String body    = readBody(req);

            if (parts.length >= 3 && "status".equals(parts[2])) {
                // PUT /api/rooms/{id}/status — all authenticated staff may change room status
                String status = new JSONObject(body).getString("roomStatus");
                sendJson(res, 200, service.updateRoomStatus(roomId, status));
            } else {
                // PUT /api/rooms/{id} — full room update (admin only)
                if (!isAdmin(req)) { sendError(res, 403, "Admin only"); return; }
                sendJson(res, 200, service.updateRoom(roomId, new JSONObject(body)));
            }
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
        if (!isAdmin(req))         { sendError(res,403,"Admin only");    return; }
        try {
            String path = req.getPathInfo(); // /{id}
            int roomId  = Integer.parseInt(path.split("/")[1]);
            sendJson(res, 200, service.deleteRoom(roomId));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}
