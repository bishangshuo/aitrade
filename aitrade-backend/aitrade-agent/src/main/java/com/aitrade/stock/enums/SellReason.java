package com.aitrade.stock.enums;

public enum SellReason {

    PROFIT("profit", "止盈卖出"),
    LOSS("loss", "止损卖出"),
    TIMEOUT("timeout", "到期卖出");

    private String code;
    private String desc;

    SellReason(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
