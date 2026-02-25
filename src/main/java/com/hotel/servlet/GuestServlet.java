package com.hotel.servlet;

import com.hotel.service.GuestService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/guests/*")
public class GuestServlet extends BaseServlet {

    private final GuestService service = new GuestService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                sendJson(res, 200, service.getAllGuests());
            } else {
                int id = Integer.parseInt(path.substring(1));
                sendJson(res, 200, service.getGuest(id));
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
        try {
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 201, service.createGuest(json));
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
            int id          = Integer.parseInt(req.getPathInfo().substring(1));
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 200, service.updateGuest(id, json));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}