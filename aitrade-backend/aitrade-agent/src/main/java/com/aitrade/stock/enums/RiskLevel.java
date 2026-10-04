package com.aitrade.stock.enums;

/**
 * 财务风险等级。
 */
public enum RiskLevel {
    /**
     * 低风险。
     */
    LOW(1, "低风险"),
    /**
     * 中风险。
     */
    MEDIUM(2, "中风险"),
    /**
     * 高风险。
     */
    HIGH(3, "高风险"),
    /**
     * 未知风险。
     */
    UNKNOWN(4, "未知风险");

    private int code;
    private String description;
    RiskLevel(int code, String description) {
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
