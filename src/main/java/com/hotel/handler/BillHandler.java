package com.hotel.handler;

import com.hotel.service.BillingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;

public class BillHandler extends BaseHandler implements HttpHandler {

    private final BillingService service = new BillingService();

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
            // POST /api/bills/generate  → generate bill
            if (method.equals("POST") && path.endsWith("/generate")) {
                String body   = readBody(ex);
                JSONObject json = new JSONObject(body);
                String resNo  = json.getString("reservationNumber");
                sendJson(ex, 201, service.generateBill(resNo));

                // POST /api/bills/payment  → process payment
            } else if (method.equals("POST") && path.endsWith("/payment")) {
                String body     = readBody(ex);
                JSONObject json = new JSONObject(body);
                sendJson(ex, 200, service.processPayment(json));

                // GET /api/bills  → all bills
            } else if (method.equals("GET") &&
                    (path.equals("/api/bills") ||
                            path.equals("/api/bills/"))) {
                sendJson(ex, 200, service.getAllBills());

                // GET /api/bills/{id}  → single bill
            } else if (method.equals("GET")) {
                int id = Integer.parseInt(
                        path.substring(path.lastIndexOf('/') + 1));
                sendJson(ex, 200, service.getBill(id));

            } else {
                sendError(ex, 405, "Method not allowed");
            }

        } catch (NumberFormatException e) {
            sendError(ex, 400, "Invalid bill ID");
        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Internal server error");
        }
    }
}