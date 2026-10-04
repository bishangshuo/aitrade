package com.aitrade.stock.enums;

/**
 * 数据质量等级。
 */
public enum DataLevel {
    FULL(1, "完整数据"),
    LIMITED_HISTORY(2, "有限历史数据"),
    DATA_GAP(3, "数据缺口"),
    DATA_ANOMALY(4, "数据异常"),
    INSUFFICIENT(5, "不足数据");

    private final int code;
    private final String description;
    DataLevel(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
