package com.hotel.servlet;

import com.hotel.service.ReportService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/api/reports/*")
public class ReportServlet extends BaseServlet {

    private final ReportService service = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        if (!isAuthenticated(req)) { sendError(res,401,"Unauthorized"); return; }

        String from = req.getParameter("from") != null
                ? req.getParameter("from")
                : LocalDate.now().minusDays(30).toString();
        String to   = req.getParameter("to") != null
                ? req.getParameter("to")
                : LocalDate.now().toString();

        String path = req.getPathInfo();

        try {
            switch (path == null ? "" : path) {
                case "/occupancy" ->
                        sendJson(res, 200, service.getOccupancyReport(from, to));
                case "/revenue"   ->
                        sendJson(res, 200, service.getRevenueReport(from, to));
                case "/guests"    ->
                        sendJson(res, 200, service.getGuestHistoryReport(from, to));
                case "/staff"     ->
                        sendJson(res, 200, service.getStaffActivityReport(from, to));
                case "/status"    ->
                        sendJson(res, 200, service.getReservationStatusReport());
                default ->
                        sendError(res, 404, "Report not found");
            }
        } catch (IllegalArgumentException e) {
            sendError(res, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); sendError(res, 500, "Server error");
        }
    }
}
//```
//
//        ---
//
//        ## Step 6 — Move HTML to `webapp/`
//
//Move all your HTML/CSS/JS files:
//        ```
//src/main/webapp/
//        ├── index.html
//├── dashboard.html
//├── css/
//        │   └── style.css
//└── js/
//        └── app.js