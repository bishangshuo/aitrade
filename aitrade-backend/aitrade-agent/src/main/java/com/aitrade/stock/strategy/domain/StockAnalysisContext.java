package com.aitrade.stock.strategy.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StockAnalysisContext {
    // 1. 籌碼面數據
    private BigDecimal profitRatio;          // 獲利籌碼比例 (例如: 0.85 代表 85%)
    private Boolean isChipSinglePeak;         // 是否形成單峰密集

}
