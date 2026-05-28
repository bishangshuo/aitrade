package com.aitrade.exchange.component;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwingStrategy {

    private final int holdDays;
    private final TradingRecord tradingRecord = new BaseTradingRecord();

    public SwingStrategy(int holdDays) {
        this.holdDays = holdDays;
    }

    public void onNewBar(BarSeries dailySeries, int currentIndex) {
        if (currentIndex < 30) {
            return;
        }

        ClosePriceIndicator close = new ClosePriceIndicator(dailySeries);
        SMAIndicator sma10 = new SMAIndicator(close, 10);
        SMAIndicator sma20 = new SMAIndicator(close, 20);
        RSIIndicator rsi = new RSIIndicator(close, 14);

        Num price = close.getValue(currentIndex);
        Num sma10Val = sma10.getValue(currentIndex);
        Num sma20Val = sma20.getValue(currentIndex);
        Num rsiVal = rsi.getValue(currentIndex);

        // ta4j 0.15 数值创建方式
        Num num70 = close.getValue(0).numOf(70);
        Num num75 = close.getValue(0).numOf(75);
        Num num1 = close.getValue(0).numOf(1);

        // ==================== 买入信号 ====================
        boolean buySignal = sma10Val.isGreaterThan(sma20Val) &&
                sma10.getValue(currentIndex - 1).isLessThanOrEqual(sma20.getValue(currentIndex - 1)) &&
                rsiVal.isLessThan(num70) &&
                tradingRecord.getCurrentPosition() == null;     // ← 0.15 关键修改点

        // ==================== 卖出信号 ====================
        boolean sellSignal = false;
        Position currentPosition = tradingRecord.getCurrentPosition();

        if (currentPosition != null && currentPosition.isOpened()) {
            long entryIndex = currentPosition.getEntry().getIndex();
            long holdDaysActual = currentIndex - entryIndex;

            sellSignal = sma10Val.isLessThan(sma20Val) ||
                    rsiVal.isGreaterThan(num75) ||
                    holdDaysActual >= holdDays + 3;
        }

        if (buySignal) {
            log.info("【买入信号】 {} | 价格: {} | 理由: SMA金叉 + RSI过滤", dailySeries.getName(), price);
            tradingRecord.enter(currentIndex, price, num1);
        }
        else if (sellSignal) {
            String reason = sma10Val.isLessThan(sma20Val) ? "SMA死叉" : "超时/超买";
            long entryIndex = currentPosition.getEntry().getIndex();
            log.info("【卖出信号】 {} | 价格: {} | 持仓: {}根日线 | 理由: {}",
                    dailySeries.getName(), price, (currentIndex - entryIndex), reason);
            tradingRecord.exit(currentIndex, price, num1);
        }
    }
}