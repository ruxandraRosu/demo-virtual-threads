package com.techconnect.service;

import com.techconnect.model.response.Trade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Uuid;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@AllArgsConstructor
@Service
public class KafkaService {

    public static final String TOPIC_NAME_TRADES = "topic-trades";
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper mapper;

    public void sendMessage(Trade trade) {
        try {
            String message = mapper.writeValueAsString(trade);
            kafkaTemplate.send(TOPIC_NAME_TRADES, Uuid.randomUuid().toString(), message)
                    .whenComplete(KafkaService::handleResult)
                    .get();
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize object", e);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception publishing message", e);
            throw new RuntimeException(e);
        }
    }

    private static void handleResult(SendResult<String, String> result, Throwable e) {
        if (e != null) {
            throw new RuntimeException("Failed to publish message", e);
        }
    }
}
