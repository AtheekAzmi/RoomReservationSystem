package com.hotel.servlet;

import com.hotel.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {

        // ── Allow cross-origin ────────────────────────────────
        res.setHeader("Access-Control-Allow-Origin",  "*");
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");

        String path = req.getPathInfo();
        System.out.println("[AuthServlet] POST path: " + path);

        try {
            if (path == null) {
                sendError(res, 404, "Path required"); return;
            }

            if (path.equals("/login")) {
                String body = readBody(req);
                System.out.println("[AuthServlet] Login body: " + body);

                JSONObject json   = new JSONObject(body);
                String     result = authService.login(json);

                System.out.println("[AuthServlet] Login result: " + result);
                sendJson(res, 200, result);

            } else if (path.equals("/logout")) {
                if (!isAuthenticated(req)) {
                    sendError(res, 401, "Unauthorized"); return;
                }
                String result = authService.logout(getToken(req));
                sendJson(res, 200, result);

            } else {
                sendError(res, 404, "Endpoint not found: " + path);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[AuthServlet] Bad request: " + e.getMessage());
            sendError(res, 400, e.getMessage());

        } catch (Exception e) {
            System.err.println("[AuthServlet] Server error: " + e.getMessage());
            e.printStackTrace();
            sendError(res, 500, "Internal server error: " + e.getMessage());
        }
    }
}