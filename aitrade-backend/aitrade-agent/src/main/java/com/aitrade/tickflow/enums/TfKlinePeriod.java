package com.aitrade.tickflow.enums;

public enum TfKlinePeriod {

    MIN_1("1m", "tf_kline_1m", 1*60*1000),
    MIN_5("5m", "tf_kline_5m", 5*60*1000),
    MIN_15("15m", "tf_kline_15m", 15*60*1000),
    MIN_30("30m", "tf_kline_30m", 30*60*1000),
    MIN_60("60m", "tf_kline_60m", 60*60*1000),
    DAY_1("1d", "tf_kline_1d", 24*60*60*1000);

    private final String code;
    private final String tableName;

    private final long duration;

    TfKlinePeriod(String code, String tableName, long duration) {
        this.code = code;
        this.tableName = tableName;
        this.duration = duration;
    }

    public String getCode() {
        return code;
    }

    public String getTableName() {
        return tableName;
    }
    public long getDuration() {
        return duration;
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
