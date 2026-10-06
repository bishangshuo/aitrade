package com.aitrade.stock.strategy.interfaces;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.tickflow.domain.TfKline;

import java.util.List;

public interface StockPickerStrategy {

    /**
     * 判斷單隻股票是否符合當前策略的選股條件
     *
     * @param stockKlines 單隻股票的歷史 K 線列表（按時間升序排列，建議長度 >= 60）
     * @return true 代表符合選股標準（預期未來 5 天上漲幅度較大）
     */
    boolean filter(List<TfKline> stockKlines);

    default boolean filter(List<TfKline> klines, StockAnalysisContext context) {
        return filter(klines);
    }

    /**
     * 獲取策略名稱
     */
    String getStrategyName();
}
