package com.example.service;

import com.example.model.MappingResolver;
import com.example.model.Match;
import com.example.model.Product;
import com.example.model.SubscribeMessage;
import com.example.model.response.Trade;
import com.example.websockets.MessageMatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Service
@Log4j2
@ConditionalOnProperty(prefix = "application.websockets", name = "enabled", havingValue = "true")
public class MatchService extends TextWebSocketHandler {

    @Value("${coinbase.endpoint}")
    private String url;
    @Value("${coinbase.matcher-message}")
    private String matcherMessage;
    private WebSocketSession clientSession;
    private final MappingResolver mappingResolver;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, SubscribeMessage> sessionsSubscriptions;
    private final MessageMatcher matcher;
    private final TradesService tradesService;
    private final RestClient restClient;


    @PostConstruct
    public void init() throws ExecutionException, InterruptedException, IOException {
        clientSession = new StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(url)).get();
        clientSession.sendMessage(new TextMessage(matcherMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws ExecutionException, InterruptedException {
        String payload = message.getPayload();
        log.info(payload);
        if (payload.contains("subscriptions"))
            return;
        handleMatchMessage(payload);

    }

    private void handleMatchMessage(String payload) throws ExecutionException, InterruptedException {
        Match match = mappingResolver.mapStringToMatch(payload);
        Trade trade = mappingResolver.mapMatchToTrade(match);
        log.info("1.1" + Thread.currentThread().getName() + " " + trade.getTradeId());
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            log.info("Before rest call {} for tradeId {}", Thread.currentThread(), trade.getTradeId());
            tradesService.decorateTrade(trade);
            log.info("After rest call {} for tradeId {}", Thread.currentThread(), trade.getTradeId());
            tradesService.publishMessage(trade);
            log.info("After kafka publish {} for tradeId {}", Thread.currentThread(), trade.getTradeId());
        });

        log.info("1.2" + Thread.currentThread().getName() + " " + trade.getTradeId());
        sessionsSubscriptions.entrySet().stream()
                .filter(e -> matcher.matches(trade, e.getValue().getFilters()))
                .forEach(e -> {
                    try {
                        log.info("2." + Thread.currentThread().getName());
                        sessions.get(e.getKey()).sendMessage(new TextMessage(mappingResolver.writeTradeToString(trade)));
                        log.info("3." + Thread.currentThread().getName());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex); //TODO
                    }
                });
    }
}
