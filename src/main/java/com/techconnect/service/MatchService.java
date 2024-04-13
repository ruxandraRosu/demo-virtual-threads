package com.techconnect.service;

import com.techconnect.model.MappingResolver;
import com.techconnect.model.Match;
import com.techconnect.model.request.SubscribeMessage;
import com.techconnect.model.response.Trade;
import com.techconnect.websockets.MessageMatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Service
@Log4j2
@ConditionalOnProperty(prefix = "application.websockets", name = "enabled", havingValue = "true")
public class MatchService extends TextWebSocketHandler {

    @Value("${coinbase.endpoint}")
    private String url;
    @Value("${coinbase.matcher-message}")
    private String matcherMessage;
    private final MappingResolver mappingResolver;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, SubscribeMessage> sessionsSubscriptions;
    private final MessageMatcher matcher;
    private final TradesService tradesService;
    private final ThreadFactory factory = Thread.ofVirtual().name("MyVirtualThread", 0L).factory();

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException, IOException {
        WebSocketSession clientSession = new StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(url)).get();
        clientSession.sendMessage(new TextMessage(matcherMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info(payload);
        if (payload.contains("subscriptions"))
            return;
        handleMatchMessage(payload);
    }

    private void handleMatchMessage(String payload) {
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
            Match match = mappingResolver.mapStringToMatch(payload);
            Trade trade = mappingResolver.mapMatchToTrade(match);
            Future<Trade> newTradeFuture = executor.submit(() -> tradesService.enrichTrade(trade));
            final Trade enrichedTrade = newTradeFuture.get();
            executor.submit(() -> tradesService.publishMessage(enrichedTrade)).get();
            sessionsSubscriptions.entrySet().stream()
                    .filter(e -> matcher.matches(enrichedTrade, e.getValue().getFilters()))
                    .forEach(e -> pushMessageToSubscriber(e.getKey(), enrichedTrade));
        } catch (Exception e) {
            log.error("Unable to handle message ", e);
        }
    }

    private void pushMessageToSubscriber(String sessionId, Trade enrichedTrade) {
        try {
            sessions.get(sessionId).sendMessage(new TextMessage(mappingResolver.writeTradeToString(enrichedTrade)));
        } catch (Exception ex) {
            log.error("Unable to publish message to subscriber", ex);
        }
    }
}
