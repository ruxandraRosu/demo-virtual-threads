package com.example.service;

import com.example.dto.MyDto;
import com.example.model.response.Trade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@AllArgsConstructor
@Service
public class KafkaService {

    public static final String TOPIC_NAME = "topic-name";
    public static final String TOPIC_NAME_TRADES = "topic-trades";
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper mapper;

    public void sendMessage(MyDto value) {
        String message = "";
        try {
            message = mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialzie object", e);
        }
        kafkaTemplate.send(TOPIC_NAME, message);
    }

    public void sendMessage(Trade trade) {
        String message = "";
        try {
            message = mapper.writeValueAsString(trade);
            kafkaTemplate.send(TOPIC_NAME_TRADES, message).get(500, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialzie object", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }
}
