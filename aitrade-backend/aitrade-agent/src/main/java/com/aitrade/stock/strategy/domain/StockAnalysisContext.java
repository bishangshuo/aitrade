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

    // 2. 集合競價與分時盤口數據
    private BigDecimal auctionCallChangeRate; // 集合競價開盤漲幅 (例如: 0.02 代表高開 2%)
    private BigDecimal auctionVolumeRatio;    // 競價匹配量較昨日放大倍數
    private Boolean isAboveAveragePriceLine;  // 分時圖回踩是否守住黃色均價線
}
