package com.hotel.util;

import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    // Build a success response
    public static String success(String message) {
        return new JSONObject()
                .put("success", true)
                .put("message", message)
                .toString();
    }

    // Build an error response
    public static String error(String message) {
        return new JSONObject()
                .put("success", false)
                .put("error",   message)
                .toString();
    }

    // Build a success response with data
    public static String success(String message, Object data) {
        return new JSONObject()
                .put("success", true)
                .put("message", message)
                .put("data",    data)
                .toString();
    }

    // Read an InputStream into a String (for reading request bodies)
    public static String readStream(InputStream is) {
        try {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    // Safely parse a JSON string — returns empty object on failure
    public static JSONObject parse(String json) {
        try {
            return new JSONObject(json);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}