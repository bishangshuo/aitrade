package com.aitrade.stock.enums;

/**
 * 风险维度。
 */
public enum RiskDimension {
    PROFITABILITY(1, "盈利能力"),
    CASH_FLOW(2, "现金流量"),
    SOLVENCY(3, "偿债能力"),
    ASSET_QUALITY(4, "资产质量"),
    PROFIT_QUALITY(5, "利润质量");

    private int code;
    private String description;

    RiskDimension(int code, String description) {
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
