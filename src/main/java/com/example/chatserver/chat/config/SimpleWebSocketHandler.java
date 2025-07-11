//package com.example.chatserver.chat.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//// connect로 웹소켓 연결 요청이 들어왔을 때 이를 처리할 클래스
//@Slf4j
//@Component
//public class SimpleWebSocketHandler extends TextWebSocketHandler {
//
//    // 연결된 세션 관리 : 스레드 safe 한 set 사용
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        sessions.add(session);
//        log.info("Connected to {}", session.getId());
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        log.info("Received message from {} : {}", session.getId(), payload);
//        for (WebSocketSession s : sessions) {
//            if(s.isOpen()) {
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        log.info("Disconnected from {}", session.getId());
//    }
//}
