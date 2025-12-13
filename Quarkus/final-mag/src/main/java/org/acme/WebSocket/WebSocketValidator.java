package org.acme.WebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;

import java.util.Base64;

@ApplicationScoped
public class WebSocketValidator {
    public String extractUserIdFromToken(Session session) {
        String queryString = session.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            String token = extractTokenFromQuery(queryString);
            return validateTokenAndGetUserId(token);
        }
        return null;
    }

    public String extractUserRoleFromToken(Session session) {
        String queryString = session.getQueryString();
        if (queryString != null && queryString.contains("token=")) {
            String token = extractTokenFromQuery(queryString);
            return validateTokenAndGetRole(token);
        }
        return null;
    }

    public boolean hasRequiredRole(String role) {
        return "user".equals(role) || "superuser".equals(role);
    }

    public String extractTokenFromQuery(String queryString) {
        String[] params = queryString.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    public String validateTokenAndGetUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // Decode JWT token manually (simple Base64 decode of payload)
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }

            // Decode the payload (second part)
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));

            // Parse JSON to extract 'sub' claim (user ID)
            // Using simple string parsing since we can't use complex JSON parsing in this context
            String userIdPattern = "\"sub\":\"";
            int startIndex = payload.indexOf(userIdPattern);
            if (startIndex == -1) {
                return null;
            }

            startIndex += userIdPattern.length();
            int endIndex = payload.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return null;
            }

            return payload.substring(startIndex, endIndex);

        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return null;
        }
    }

    public String validateTokenAndGetRole(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // Decode JWT token manually
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }

            // Decode the payload
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));

            // Extract roles - this depends on your OIDC configuration
            // Common patterns: "realm_access":{"roles":["user"]} or "roles":["user"]
            if (payload.contains("\"user\"") || payload.contains("\"superuser\"")) {
                // Simple role detection - you might need to adjust this based on your JWT structure
                if (payload.contains("\"superuser\"")) {
                    return "superuser";
                } else if (payload.contains("\"user\"")) {
                    return "user";
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error extracting role from token: " + e.getMessage());
            return null;
        }
    }
}
