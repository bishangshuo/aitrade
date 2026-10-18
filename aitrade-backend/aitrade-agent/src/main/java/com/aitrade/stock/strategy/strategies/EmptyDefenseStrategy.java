package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.enums.StrategyType;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmptyDefenseStrategy implements StockPickerStrategy {

    @Override
    public Integer getStrategyCode() {
        return StrategyType.EMPTY_DEFENSE.getCode();
    }

    @Override
    public String getStrategyName() {
        return "大盤風險期-強制空倉防禦策略";
    }

    @Override
    public boolean filter(List<TfKline> klines) {
        // 大盤處於高風險態勢（主跌、陰跌、高位派發或衝顶高潮），不做任何個股突破買入
        return false;
    }

    @Override
    public boolean filter(List<TfKline> klines, StockAnalysisContext context) {
        return false;
    }

    @Override
    public StockPickDiagnostic analyze(String symbol, String stockName, LocalDate tradeDate, List<TfKline> klines, StockAnalysisContext context) {
        return null;
    }
}
