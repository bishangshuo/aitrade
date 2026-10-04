package com.aitrade.tickflow.enums;

public enum TfKlinePeriod {

    MIN_1("1m", "tf_kline_1m"),
    MIN_5("5m", "tf_kline_5m"),
    MIN_15("15m", "tf_kline_15m"),
    MIN_30("30m", "tf_kline_30m"),
    MIN_60("60m", "tf_kline_60m"),
    DAY_1("1d", "tf_kline_1d");

    private final String code;
    private final String tableName;

    TfKlinePeriod(String code, String tableName) {
        this.code = code;
        this.tableName = tableName;
    }

    public String getCode() {
        return code;
    }

    public String getTableName() {
        return tableName;
    }

    public static TfKlinePeriod fromCode(String code) {
        for (TfKlinePeriod period : values()) {
            if (period.code.equalsIgnoreCase(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Unsupported kline period: " + code);
    }
}
