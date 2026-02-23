package com.hotel.handler;

import com.hotel.service.ReportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportHandler extends BaseHandler implements HttpHandler {

    private final ReportService service = new ReportService();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            handleCors(ex); return;
        }
        if (!isAuthenticated(ex)) {
            sendError(ex, 401, "Unauthorized"); return;
        }

        String path = ex.getRequestURI().getPath();
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());

        // Default date range: last 30 days to today
        String from = params.getOrDefault("from",
                LocalDate.now().minusDays(30).toString());
        String to   = params.getOrDefault("to",
                LocalDate.now().toString());

        try {
            if (path.endsWith("/occupancy")) {
                sendJson(ex, 200, service.getOccupancyReport(from, to));

            } else if (path.endsWith("/revenue")) {
                sendJson(ex, 200, service.getRevenueReport(from, to));

            } else if (path.endsWith("/guests")) {
                sendJson(ex, 200, service.getGuestHistoryReport(from, to));

            } else if (path.endsWith("/staff")) {
                sendJson(ex, 200, service.getStaffActivityReport(from, to));

            } else if (path.endsWith("/status")) {
                sendJson(ex, 200, service.getReservationStatusReport());

            } else {
                sendError(ex, 404, "Report not found. Available: " +
                        "/occupancy, /revenue, /guests, /staff, /status");
            }

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Internal server error");
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}
//```
//
//        ---
//
//        ## ✅ Final Handler Package Structure
//```
//com/hotel/handler/
//        ├── BaseHandler.java          ✅
//        ├── StaticFileHandler.java    ✅
//        ├── AuthHandler.java          ✅
//        ├── ReservationHandler.java   ✅
//        ├── RoomHandler.java          ✅
//        ├── GuestHandler.java         ✅
//        ├── BillHandler.java          ✅
//        ├── StaffHandler.java         ✅
//        └── ReportHandler.java        ✅
//        ```
//
//After adding all these, `Main.java` will compile. Run it and you should see:
//        ```
//Hotel System running at http://localhost:8080