package com.aitrade.stock.strategy.factory;


import com.aitrade.stock.enums.MarketRegimeEnum;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.stock.strategy.strategies.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StockStrategyFactory {

    @Autowired private StrongBreakoutStrategy strongBreakoutStrategy;
    @Autowired private PullbackLowSuckStrategy pullbackLowSuckStrategy;
    @Autowired private BottomBreakoutStrategy bottomBreakoutStrategy;
    @Autowired private OversoldReboundStrategy oversoldReboundStrategy;
    @Autowired private EmptyDefenseStrategy emptyDefenseStrategy;

    private final Map<MarketRegimeEnum, StockPickerStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 主升浪 -> 強勢突破
        strategyMap.put(MarketRegimeEnum.STRONG_UPTREND, strongBreakoutStrategy);

        // 震盪攀升 / 箱體震盪 -> 低吸回踩
        strategyMap.put(MarketRegimeEnum.WEAK_UPTREND, pullbackLowSuckStrategy);
        strategyMap.put(MarketRegimeEnum.RANGE_BOUND, pullbackLowSuckStrategy);

        // 低位築底 -> 放量首板
        strategyMap.put(MarketRegimeEnum.LOW_CONSOLIDATION, bottomBreakoutStrategy);

        // 超跌反彈 -> 極限抄底
        strategyMap.put(MarketRegimeEnum.REBOUND_DOWNTREND, oversoldReboundStrategy);

        // 高風險態勢 -> 強制空倉
        strategyMap.put(MarketRegimeEnum.STRONG_DOWNTREND, emptyDefenseStrategy);
        strategyMap.put(MarketRegimeEnum.WEAK_DOWNTREND, emptyDefenseStrategy);
        strategyMap.put(MarketRegimeEnum.HIGH_CONSOLIDATION, emptyDefenseStrategy);
        strategyMap.put(MarketRegimeEnum.CLIMAX_UPTREND, emptyDefenseStrategy);
    }

    /**
     * 根據大盤態勢獲取最佳選股策略
     */
    public StockPickerStrategy getStrategy(MarketRegimeEnum regime) {
        return strategyMap.getOrDefault(regime, emptyDefenseStrategy);
    }
}