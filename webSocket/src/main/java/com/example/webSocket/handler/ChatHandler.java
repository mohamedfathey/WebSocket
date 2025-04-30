package com.example.webSocket.handler;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.*;

public class ChatHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new HashMap<>(); // Key: userId
    private static final Map<String, WebSocketSession> adminSessions = new HashMap<>(); // Key: adminId
    private static final Map<String, String> userToAdminMapping = new HashMap<>(); // Key: userId, Value: adminId

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String role = getRoleFromSession(session);
        String userIdOrAdminId = getUserIdOrAdminIdFromSession(session);

        System.out.println("New connection established. Role: " + role + ", ID: " + userIdOrAdminId);

        if ("user".equals(role)) {
            String adminId = getAdminIdFromSession(session);
            System.out.println("User connected. Admin ID: " + adminId);
            System.out.println("Current admin sessions: " + adminSessions);

            if (adminId != null && adminSessions.containsKey(adminId)) {
                userToAdminMapping.put(userIdOrAdminId, adminId); // Map user to admin
                userSessions.put(userIdOrAdminId, session); // Store user session
                session.sendMessage(new TextMessage("Connected as USER. Assigned to ADMIN: " + adminId));
                System.out.println("User assigned to admin: " + adminId);
            } else {
                session.sendMessage(new TextMessage("Admin not found or not available."));
                session.close();
                System.out.println("Admin not found or not available. Closing user session.");
            }
        } else if ("admin".equals(role)) {
            adminSessions.put(userIdOrAdminId, session); // Store admin session
            session.sendMessage(new TextMessage("Connected as ADMIN. Waiting for users."));
            System.out.println("Admin connected. ID: " + userIdOrAdminId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String senderId = getUserIdOrAdminIdFromSession(session);
        String role = getRoleFromSession(session);
        String payload = message.getPayload();

        if ("user".equals(role)) {
            String assignedAdmin = userToAdminMapping.get(senderId);
            if (assignedAdmin != null && adminSessions.containsKey(assignedAdmin)) {
                adminSessions.get(assignedAdmin).sendMessage(new TextMessage("User " + senderId + ": " + payload));
            } else {
                session.sendMessage(new TextMessage("No assigned admin is available."));
            }
        } else if ("admin".equals(role)) {
            String assignedUser = getUserAssignedToAdmin(senderId);
            if (assignedUser != null && userSessions.containsKey(assignedUser)) {
                userSessions.get(assignedUser).sendMessage(new TextMessage("Admin " + senderId + ": " + payload));
            } else {
                session.sendMessage(new TextMessage("No user is assigned to you currently."));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userIdOrAdminId = getUserIdOrAdminIdFromSession(session);
        if (userSessions.containsKey(userIdOrAdminId)) {
            userToAdminMapping.remove(userIdOrAdminId);
            userSessions.remove(userIdOrAdminId);
        } else if (adminSessions.containsKey(userIdOrAdminId)) {
            adminSessions.remove(userIdOrAdminId);
            userToAdminMapping.values().remove(userIdOrAdminId);
        }
        System.out.println("Connection closed. ID: " + userIdOrAdminId);
    }

    private String getRoleFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.contains("role=admin") ? "admin" : "user";
    }

    private String getAdminIdFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        if (uri.contains("adminId=")) {
            return uri.split("adminId=")[1].split("&")[0]; // Extract adminId from URI
        }
        return null;
    }

    private String getUserIdOrAdminIdFromSession(WebSocketSession session) {
        String uri = session.getUri().toString();
        if (uri.contains("userId=")) {
            return uri.split("userId=")[1].split("&")[0]; // Extract userId from URI
        } else if (uri.contains("adminId=")) {
            return uri.split("adminId=")[1].split("&")[0]; // Extract adminId from URI
        }
        return session.getId(); // Fallback to session ID
    }

    private String getUserAssignedToAdmin(String adminId) {
        for (Map.Entry<String, String> entry : userToAdminMapping.entrySet()) {
            if (entry.getValue().equals(adminId)) {
                return entry.getKey();
            }
        }
        return null;
    }
}