package com.example.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Trade {

    private String gridKey;
    private String price;
    private String type;
    private String size;
    private String side;
    private String datetime;
    private String time;
    private String date;
    private String makerOrderId;
    private String takerOrderId;
    private String tradeId;
    private Long sequence;
    private String displayName;
    private String base;

}
