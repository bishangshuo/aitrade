package com.aitrade.stock.enums;

/**
 * 交易方向类型
 */
public enum TradeType {
    BUY("BUY", "买入"),
    SELL("SELL", "卖出");

    private String value;
    private String name;


    TradeType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }
    public String getName() {
        return name;
    }
}
