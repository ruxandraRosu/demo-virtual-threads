package com.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
@Slf4j
public class MyService {

    private RestClient restClient;

    public void callExternalService(int seconds) {

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {

            ResponseEntity<Void> result = restClient.get()
                    .uri("/block/" + seconds)
                    .retrieve()
                    .toBodilessEntity();

            log.info("{} on {}", result.getStatusCode(), Thread.currentThread());
        });

    }

}
