package com.aitrade.stock.enums;

/**
 * 报告期类型。
 */
public enum ReportPeriodType {
    Q1(1, "一季報"),
    H1(2, "中報"),
    Q3(3, "三季報"),
    FY(4, "年报"),
    UNKNOWN(0, "未知");

    private int code;
    private String description;
    ReportPeriodType(int code, String description) {
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
