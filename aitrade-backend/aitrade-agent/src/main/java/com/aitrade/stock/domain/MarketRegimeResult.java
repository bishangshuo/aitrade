package com.aitrade.stock.domain;

import com.aitrade.stock.enums.MarketRegimeEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MarketRegimeResult {
    private MarketRegimeEnum regime;
    private String regimeName;
    private String description;
    private String strategy;

    // 量化特徵指標
    private BigDecimal latestClose;
    private BigDecimal bias20Percent;         // BIAS20 乖離率 (%)
    private BigDecimal ma20SlopePercent;     // MA20 5日斜率 (%)
    private BigDecimal ma60SlopePercent;     // MA60 5日斜率 (%)
    private BigDecimal position250dPercent;   // 250日相對高低位 (%)
    private BigDecimal bollBandwidthPercent;  // 布林帶帶寬 (%)
    private BigDecimal range20dPercent;      // 20日振幅 (%)
    private BigDecimal rsi14;                 // RSI(14)
}