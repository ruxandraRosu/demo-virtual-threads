package com.techconnect.configuration;

import com.techconnect.model.request.SubscribeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconnect.websockets.TradeHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
@ConditionalOnProperty(prefix = "application.websockets", name = "enabled", havingValue = "true")
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, SubscribeMessage> sessionsSubscriptions= new ConcurrentHashMap<>();


    @Bean("sessions")
    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    @Bean("sessionsSubscriptions")
    public Map<String, SubscribeMessage> getSessionsSubscriptions() {
        return sessionsSubscriptions;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TradeHandler(sessions, sessionsSubscriptions, new ObjectMapper()), "/feed/trades");
    }

}
