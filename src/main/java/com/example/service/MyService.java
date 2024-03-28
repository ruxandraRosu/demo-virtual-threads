package com.example.service;

import com.example.dto.MyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@Slf4j
public class MyService {

    private RestClient restClient;
    private final KafkaService kafkaService;
    private ObjectMapper mapper;

    public void callExternalService(int seconds) {

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {

            ResponseEntity<Void> result = restClient.get()
                    .uri("/block/" + seconds)
                    .retrieve()
                    .toBodilessEntity();

            log.info("{} on {}", result.getStatusCode(), Thread.currentThread());
        });

    }

    public void publishMessage(MyDto message) {
        kafkaService.sendMessage(message);
    }


    public void doWork(List<MyDto> list) {
        int sum = 0; //TODO
        IntStream.range(1, 10)
                .mapToObj(i -> new DigestUtils("SHA-256").digestAsHex(i + list.get(0).toString()))
                .forEach(v -> log.info("Signature {}", v));

    }

}
