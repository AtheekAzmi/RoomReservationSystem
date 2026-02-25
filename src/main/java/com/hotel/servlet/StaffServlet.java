package com.hotel.servlet;

import com.hotel.service.StaffService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/staff/*")
public class StaffServlet extends BaseServlet {

    private final StaffService service = new StaffService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        if (!isAdmin(req))         { sendError(res,403,"Admin only");    return; }
        try {
            sendJson(res, 200, service.getAllStaff());
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
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 201, service.createStaff(json));
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
        if (!isAdmin(req))         { sendError(res,403,"Admin only");    return; }
        try {
            int id          = Integer.parseInt(req.getPathInfo().substring(1));
            JSONObject json = new JSONObject(readBody(req));
            sendJson(res, 200, service.updateStaff(id, json));
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
            int id = Integer.parseInt(req.getPathInfo().substring(1));
            sendJson(res, 200, service.deleteStaff(id));
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}