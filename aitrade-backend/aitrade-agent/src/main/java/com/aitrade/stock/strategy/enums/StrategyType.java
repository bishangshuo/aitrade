package com.aitrade.stock.strategy.enums;

public enum StrategyType {
    STRONG_BREAKOUT(1, "主升浪-VCP/均線粘合/空中加油多維共振突破策略"),
    BOTTOM_BREAKOUT(2, "底部反轉-趨勢動能量價共振突破策略"),
    PULLBACK_LOW_SUCK(3, "震盪攀升-趨勢動能量價共振低吸策略"),
    OVERSOLD_REBOUND(4, "超跌反彈-趨勢動能量價共振反彈策略"),
    EMPTY_DEFENSE(5, "大盤風險期-強制空倉防禦策略");

    private int code;
    private String description;

    StrategyType(int code, String description) {
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
