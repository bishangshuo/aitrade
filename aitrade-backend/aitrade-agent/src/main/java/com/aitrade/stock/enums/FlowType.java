package com.aitrade.stock.enums;

/**
 * 流水类型
 * DEPOSIT-入金, WITHDRAW-出金, BUY-買入扣款, SELL-賣出回款, COMMISSION-傭金, TRANSFERFEE-过户费， TAX-印花稅
 */
public enum FlowType {
    DEPOSIT("DIPOSIT", "入金"),
    WITHDRAW("WITHDRAW", "出金"),
    BUY("BUY", "買入扣款"),
    SELL("SELL", "賣出回款"),
    COMMISSION("COMMISSION", "傭金"),
    TRANSFERFEE("TRANSFERFEE", "过户费"),
    TAX("TAX", "印花稅");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    FlowType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
