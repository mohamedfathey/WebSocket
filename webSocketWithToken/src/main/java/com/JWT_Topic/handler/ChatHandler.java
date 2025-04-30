package com.JWT_Topic.handler;

import com.JWT_Topic.service.JWTService;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {

    private final JWTService jwtService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> offlineMessages = new ConcurrentHashMap<>();

    public ChatHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getParam(session, "token");
        String username = jwtService.getUsername(token);
        sessions.put(username, session);

        // Send offline messages if any
        List<String> messages = offlineMessages.getOrDefault(username, new ArrayList<>());
        for (String msg : messages) {
            session.sendMessage(new TextMessage(msg));
        }
        offlineMessages.remove(username); // Clear after sending
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String token = getParam(session, "token");
        String sender = jwtService.getUsername(token);
        String senderRole = jwtService.getRole(token);
        String target = getParam(session, "targetUsername");

        if (!isAllowed(senderRole, jwtService.getRoleByUsername(target))) {
            session.sendMessage(new TextMessage("❌ Communication not allowed with this role."));
            return;
        }

        String fullMessage = "[" + sender + "] → " + message.getPayload();
        WebSocketSession targetSession = sessions.get(target);

        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(fullMessage));
        } else {
            offlineMessages.computeIfAbsent(target, k -> new ArrayList<>()).add(fullMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().removeIf(s -> s.equals(session));
    }

    // ========== Helper Methods ==========

    private String getParam(WebSocketSession session, String key) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals(key)) {
                return parts[1];
            }
        }
        return null;
    }

    private boolean isAllowed(String senderRole, String targetRole) {
        if (senderRole.equals("user") && targetRole.equals("user")) return false;
        return true;
    }
}
