package com.example.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder){
        return restClientBuilder
                .baseUrl("http://localhost:8084")
                .build();
    }

       @Bean
    public ProducerFactory<String, String> producerFactory(){
           Map<String, Object> configProps = new HashMap<>();
           configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
           configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
           configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
           return new DefaultKafkaProducerFactory<>(configProps);
       }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> factory) {
        return new KafkaTemplate<>(factory);
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
