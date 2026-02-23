package com.hotel.handler;

import com.sun.net.httpserver.HttpExchange;
import com.hotel.util.SessionManager;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class BaseHandler {

    protected String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    protected void sendJson(HttpExchange ex, int status, String json)
            throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",
                "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods",
                "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Content-Type,Authorization");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendError(HttpExchange ex, int code, String message)
            throws IOException {
        String json = new JSONObject().put("error", message).toString();
        sendJson(ex, code, json);
    }

    protected String getToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer "))
            return auth.substring(7).trim();
        return null;
    }

    protected boolean isAuthenticated(HttpExchange ex) {
        return SessionManager.getInstance().isValid(getToken(ex));
    }

    protected boolean isAdmin(HttpExchange ex) {
        return SessionManager.getInstance().isAdmin(getToken(ex));
    }

    protected void handleCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods",
                "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Content-Type,Authorization");
        ex.sendResponseHeaders(204, -1);
    }
}