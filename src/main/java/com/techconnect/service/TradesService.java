package com.techconnect.service;

import com.techconnect.model.Product;
import com.techconnect.model.response.ProductInfo;
import com.techconnect.model.Stats;
import com.techconnect.model.response.Trade;
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
    private final Product emptyProduct = new Product();
    private final Stats emptyStats = new Stats();

    public Product getProduct(String productId) {
        try {
            return restClient.get()
                    .uri("/products/" + productId)
                    .retrieve()
                    .toEntity(Product.class)
                    .getBody();
        } catch (Exception e) {
            log.error("Exception retrieving product: "+ e.getMessage());
            return emptyProduct;
        }
    }

    public Stats getProductStats(String productId) {
        log.info("{} retrieving stats", Thread.currentThread());
       try {
           return restClient.get()
                   .uri("/products/" + productId + "/stats")
                   .retrieve()
                   .toEntity(Stats.class)
                   .getBody();
       }
       catch (Exception e){
           log.error("Exception retrieving stats: "+ e.getMessage());
           return emptyStats;
       }
    }

    public Trade enrichTrade(Trade trade) {
        Product product = getProduct(trade.getGridKey());
        return trade.toBuilder()
                .base(product.getBaseCurrency())
                .displayName(product.getDisplayName())
                .build();
    }

    public void publishMessage(Trade message) {
        log.info("{} publishing", Thread.currentThread());
        kafkaService.sendMessage(message);
        log.info("{} completed publishing", Thread.currentThread());
    }


    public ProductInfo getProductInfo(String productId) {
        Stats stats = getProductStats(productId);
        Product product = getProduct(productId);
        return ProductInfo.builder().product(product).stats(stats).build();
    }
}
