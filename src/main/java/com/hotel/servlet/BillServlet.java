package com.hotel.servlet;

import com.hotel.service.BillingService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/bills/*")
public class BillServlet extends BaseServlet {

    private final BillingService service = new BillingService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                sendJson(res, 200, service.getAllBills());
            } else {
                int id = Integer.parseInt(path.substring(1));
                sendJson(res, 200, service.getBill(id));
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
            String path     = req.getPathInfo();
            JSONObject json = new JSONObject(readBody(req));

            // POST /api/bills/generate
            if ("/generate".equals(path)) {
                String resNo = json.getString("reservationNumber");
                sendJson(res, 201, service.generateBill(resNo));

                // POST /api/bills/payment
            } else if ("/payment".equals(path)) {
                sendJson(res, 200, service.processPayment(json));

            } else {
                sendError(res, 404, "Not found");
            }
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}