package com.hotel.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.file.Files;

public class StaticFileHandler extends BaseHandler implements HttpHandler {

    // Folder where your HTML/CSS/JS files are
    private static final String WEB_ROOT = "src/main/resources/web";

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();

        // Default to index.html
        if (path.equals("/")) path = "/index.html";

        File file = new File(WEB_ROOT + path);

        // If file not found, serve index.html (for SPA routing)
        if (!file.exists() || file.isDirectory()) {
            file = new File(WEB_ROOT + "/index.html");
        }

        if (!file.exists()) {
            String notFound = "404 - Page Not Found";
            ex.sendResponseHeaders(404, notFound.length());
            ex.getResponseBody().write(notFound.getBytes());
            return;
        }

        String mimeType = getMimeType(file.getName());
        byte[] bytes    = Files.readAllBytes(file.toPath());

        ex.getResponseHeaders().set("Content-Type", mimeType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css"))  return "text/css";
        if (fileName.endsWith(".js"))   return "application/javascript";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".png"))  return "image/png";
        if (fileName.endsWith(".jpg") ||
                fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}