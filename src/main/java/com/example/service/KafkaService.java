package com.example.service;

import com.example.dto.MyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class KafkaService {

    public static final String TOPIC_NAME = "topic-name";
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;

    public void sendMessage(MyDto value) {
        String message = "";
        try {
            message = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object", e);
        }
        kafkaTemplate.send(TOPIC_NAME, message);
    }
}
