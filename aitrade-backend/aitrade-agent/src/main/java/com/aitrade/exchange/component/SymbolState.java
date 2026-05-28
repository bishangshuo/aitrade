package com.aitrade.exchange.component;

import com.aitrade.exchange.domain.Kline;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.aggregator.BaseBarSeriesAggregator;
import org.ta4j.core.aggregator.DurationBarAggregator;
import org.ta4j.core.num.DecimalNum;   // ← 新增导入

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Slf4j
public class SymbolState implements Serializable {

    /** 交易对，如 BTC-USDT */
    private String symbol;

    /** WebSocket 最后收到的时间戳（用于缺失检测） */
    private volatile long lastTimestamp = 0;

    /** 当前正在处理的K线时间戳（用于判断是否进入新K线周期） */
    private volatile long currentKlineTime = 0;

    /** 上一根已确认的K线数据（用于触发策略信号） */
    private volatile Kline lastConfirmedKline = null;

    // ==================== 新增 TA4J 相关字段 ====================
    private final BarSeries series15m;
    private BarSeries seriesDaily;

    private ClosePriceIndicator closeDaily;
    private SMAIndicator sma10Daily;
    private SMAIndicator sma20Daily;
    private RSIIndicator rsi14Daily;
    private ATRIndicator atr14Daily;

    private final SwingStrategy strategy;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public SymbolState(String symbol) {
        this.symbol = symbol;
        this.series15m = new BaseBarSeriesBuilder()
                .withName(symbol + "_15m")
                .withMaxBarCount(110000)
                .build();
        this.strategy = new SwingStrategy(12);   // 默认12天波段
    }

    // ==================== TA4J 方法 ====================

    /** 历史数据批量加载（补偿恢复时调用） */
    public void loadHistoricalBars(List<Kline> klines) {
        for (Kline k : klines) {
            if (Boolean.TRUE.equals(k.getIsFinal())) {
                series15m.addBar(createBar(k));
            }
        }
        initializeIndicators();
        initialized.set(true);
        log.info("[{}] TA4J 历史数据加载完成，共 {} 根15m K线", symbol, series15m.getBarCount());
    }

    /** 实时新增K线（仅最终确认） */
    public void addBar(Kline kline, boolean isFinal) {
        if (!isFinal) return;

        series15m.addBar(createBar(kline));

        if (initialized.get()) {
            BarSeries daily = getDailySeries();
            int endIndex = daily.getEndIndex();
            strategy.onNewBar(daily, endIndex);
        }
    }

    /** 创建 Bar（适配 ta4j 0.15） */
    private Bar createBar(Kline k) {
        ZonedDateTime zonedTime = Instant.ofEpochMilli(k.getOpenTime())
                .atZone(ZoneId.of("UTC"));

        return BaseBar.builder()
                .timePeriod(Duration.ofMinutes(15))
                .endTime(zonedTime)
                .openPrice(DecimalNum.valueOf(k.getOpen()))
                .highPrice(DecimalNum.valueOf(k.getHigh()))
                .lowPrice(DecimalNum.valueOf(k.getLow()))
                .closePrice(DecimalNum.valueOf(k.getClose()))
                .volume(DecimalNum.valueOf(k.getVolume()))
                .build();
    }

    private void initializeIndicators() {
        BarSeries daily = getDailySeries();
        closeDaily = new ClosePriceIndicator(daily);
        sma10Daily = new SMAIndicator(closeDaily, 10);
        sma20Daily = new SMAIndicator(closeDaily, 20);
        rsi14Daily = new RSIIndicator(closeDaily, 14);
        atr14Daily = new ATRIndicator(daily, 14);
    }

    public BarSeries getDailySeries() {
        if (seriesDaily == null) {
            seriesDaily = new BaseBarSeriesAggregator(
                    new DurationBarAggregator(Duration.ofDays(1), true))
                    .aggregate(series15m, symbol + "_daily");
        }
        return seriesDaily;
    }

    public SwingStrategy getStrategy() {
        return strategy;
    }
}