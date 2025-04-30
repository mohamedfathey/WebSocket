//package com.example.webSocket.handler;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.WebSocketMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
// public class TutorialHandler implements WebSocketHandler {
//    private static final Logger log = LoggerFactory.getLogger(TutorialHandler.class);
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        log.info("Connection established on session: {}", session.getId());
//
//    }
//
//    @Override
//    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//        String tutorial = (String) message.getPayload();
//        log.info("Message: {}", tutorial);
//        session.sendMessage(new TextMessage( tutorial));
//        Thread.sleep(1000);
//        session.sendMessage(new TextMessage("Completed processing tutorial: " + tutorial));
//
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        log.info("Exception occured: {} on session: {}", exception.getMessage(), session.getId());
//
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//        log.info("Connection closed on session: {} with status: {}", session.getId(), closeStatus.getCode());
//
//    }
//
//    @Override
//    public boolean supportsPartialMessages() {
//        return false;
//    }
//
//}