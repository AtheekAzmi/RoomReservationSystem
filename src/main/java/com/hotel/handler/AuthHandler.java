package com.hotel.handler;

import com.hotel.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.IOException;

public class AuthHandler extends BaseHandler implements HttpHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // Handle CORS preflight
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            handleCors(ex); return;
        }

        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();

        try {
            // POST /api/auth/login
            if (method.equals("POST") && path.endsWith("/login")) {
                String body   = readBody(ex);
                JSONObject json = new JSONObject(body);
                String result   = authService.login(json);
                sendJson(ex, 200, result);

                // POST /api/auth/logout
            } else if (method.equals("POST") && path.endsWith("/logout")) {
                if (!isAuthenticated(ex)) {
                    sendError(ex, 401, "Unauthorized"); return;
                }
                String result = authService.logout(getToken(ex));
                sendJson(ex, 200, result);

            } else {
                sendError(ex, 404, "Endpoint not found");
            }

        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "Internal server error");
        }
    }
}