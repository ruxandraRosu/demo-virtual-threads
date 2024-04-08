package com.techconnect.service;

import com.techconnect.model.Product;
import com.techconnect.model.ProductInfo;
import com.techconnect.model.Stats;
import com.techconnect.model.response.Trade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@AllArgsConstructor
@Slf4j
public class TradesService {

    private RestClient restClient;
    private final KafkaService kafkaService;
    private ObjectMapper mapper;

    public Product getProduct(String productId) {
        return restClient.get()
                .uri("/products/" + productId)
                .retrieve()
                .toEntity(Product.class)
                .getBody();
    }

    public Stats getProductStats(String productId) {
        log.info("Before rest call {} for productId {}", Thread.currentThread(), productId);
        return restClient.get()
                .uri("/products/" + productId + "/stats")
                .retrieve()
                .toEntity(Stats.class)
                .getBody();
    }

    public void decorateTrade(Trade trade) {
        Product product = getProduct(trade.getGridKey());
        trade.setBase(product.getBaseCurrency());
        trade.setDisplayName(product.getDisplayName());
    }

    public void publishMessage(Trade message) {
        log.info("Before kafka publishing  {}", Thread.currentThread());
        kafkaService.sendMessage(message);
        log.info("After kafka publish {}", Thread.currentThread());
    }


    public ProductInfo getProductInfo(String productId) {
        Stats stats = getProductStats(productId);
        Product product = getProduct(productId);
        return ProductInfo.builder().product(product).stats(stats).build();


    }
}
