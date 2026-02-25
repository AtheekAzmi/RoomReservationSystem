package com.hotel.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.hotel.util.SessionManager;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public abstract class BaseServlet extends HttpServlet {

    protected void sendJson(HttpServletResponse res,
                            int status, String json) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        setCorsHeaders(res);
        PrintWriter out = res.getWriter();
        out.print(json);
        out.flush();
    }

    protected void sendError(HttpServletResponse res,
                             int code, String message) throws IOException {
        String json = new JSONObject()
                .put("error",   message)
                .put("code",    code)
                .toString();
        sendJson(res, code, json);
    }

    protected String readBody(HttpServletRequest req) throws IOException {
        try {
            return req.getReader().lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            return "";
        }
    }

    protected String getToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        System.out.println("[BaseServlet] Authorization header: " +
                (auth != null ? auth.substring(0, Math.min(30, auth.length())) : "NULL"));
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    protected boolean isAuthenticated(HttpServletRequest req) {
        String token = getToken(req);
        boolean valid = SessionManager.getInstance().isValid(token);
        System.out.println("[BaseServlet] isAuthenticated: " + valid +
                " token=" + (token != null ? token.substring(0,8)+"..." : "null"));
        return valid;
    }

    protected boolean isAdmin(HttpServletRequest req) {
        return SessionManager.getInstance().isAdmin(getToken(req));
    }

    private void setCorsHeaders(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",  "*");
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest req,
                             HttpServletResponse res) throws IOException {
        setCorsHeaders(res);
        res.setStatus(204);
    }
}
//```
//
//        ---
//
//        ## Step 5 — Full Test Flow
//
//After fixing, do this:
//        ```
//        1. Stop Tomcat
//2. Build → Rebuild Project
//3. Start Tomcat
//4. Open http://localhost:8080
//        5. Login with admin / Admin@123
//        6. F12 Console → should see:
//        [Login] Calling: http://localhost:8080/api/auth/login
//        [Login] Status: 200
//        [Login] Token saved: YES
//   [Login] Redirecting to dashboard...