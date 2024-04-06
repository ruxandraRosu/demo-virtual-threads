package com.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductInfo {

    private String open;
    private String high;
    private String low;
    private String volume;
    private String displayName;
    private String baseCurrency;

    public static class ProductInfoBuilder {

        public ProductInfoBuilder product(Product product) {
            this.baseCurrency = product.getBaseCurrency();
            this.displayName = product.getDisplayName();
            return this;
        }

        public ProductInfoBuilder stats(Stats stats) {
            this.high = stats.getHigh();
            this.low = stats.getLow();
            this.open = stats.getOpen();
            this.volume = stats.getVolume();
            return this;
        }

    }

}
