package com.techconnect.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconnect.model.request.SubscribeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;

@RequiredArgsConstructor
public class TradeHandler extends AbstractWebSocketHandler {
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, SubscribeMessage> sessionsSubscriptions;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionsSubscriptions.remove(session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        SubscribeMessage subscribeMessage = mapStringToMessage(message.getPayload().toString());
        sessionsSubscriptions.put(session.getId(), subscribeMessage);
    }

    public SubscribeMessage mapStringToMessage(String payloadString) {
        try {
            return objectMapper.readValue(payloadString, SubscribeMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
