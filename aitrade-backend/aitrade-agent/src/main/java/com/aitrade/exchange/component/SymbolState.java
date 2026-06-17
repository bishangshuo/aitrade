package com.aitrade.exchange.component;

import com.aitrade.exchange.domain.Kline;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
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

    /** 交易對，如 BTC-USDT */
    private String symbol;

    // ==================== TA4J 核心數據序列 ====================
    private final BarSeries series15m;
    private BarSeries seriesDaily;

    /** 內聚的策略核心（指標快取與風控全部在策略內部實現） */
    private final SwingStrategy strategy;
    // 在 SymbolState 類別中補回 volatile 欄位
    private volatile long lastTimestamp = 0;

    public SymbolState(String symbol) {
        this.symbol = symbol;
        this.series15m = new BaseBarSeriesBuilder()
                .withName(symbol + "_15m")
                .withMaxBarCount(110000) // 防止內存溢出
                .build();
        this.strategy = new SwingStrategy(12); // 默認12天波段策略
    }

    // ==================== TA4J 數據流核心方法 ====================

    /** 歷史數據批量加載（補償恢復時調用） */
    public void loadHistoricalBars(List<Kline> klines) {
        for (Kline k : klines) {
            if (Boolean.TRUE.equals(k.getIsFinal())) {
                addBarToSeries(k); // 呼叫防禦性添加
            }
        }

        // 歷史數據塞滿後，主動觸發第一次聚合
        this.seriesDaily = refreshDailySeries();
        log.info("[{}] TA4J 歷史數據加載完成，共 {} 根15m K線", symbol, series15m.getBarCount());
    }

    /** * 純粹的聚合工具方法
     */
    private BarSeries refreshDailySeries() {
        return new BaseBarSeriesAggregator(
                // 將 true 改為 false，讓不滿 24 小時的當天數據也能強制聚合出一根日線
                new DurationBarAggregator(Duration.ofDays(1), false))
                .aggregate(series15m, symbol + "_daily");
    }

    /** 即時新增K線（僅最終確認） */
    public void addBar(Kline kline, boolean isFinal) {
        if (!isFinal) return;

        // 獲取添加前的最後一根時間
        ZonedDateTime lastTimeBefore = series15m.getBarCount() > 0 ? series15m.getLastBar().getEndTime() : null;

        if (addBarToSeries(kline)) {
            ZonedDateTime lastTimeAfter = series15m.getLastBar().getEndTime();

            // 關鍵效能優化：如果新 K線導致日期變更（跨天），或者 seriesDaily 還沒初始化
            if (seriesDaily == null || lastTimeBefore == null || lastTimeAfter.toLocalDate().isAfter(lastTimeBefore.toLocalDate())) {
                this.seriesDaily = refreshDailySeries(); // 跨天了，重新聚合
            } else {
                // 如果在同一天內，為了讓策略能看到「今天最新的即時收盤價變動」，我們也需要刷新
                // 註：這裏可以根據效能微調，若追求 $O(1)$，通常會在實戰中改用手動 append 最終價
                this.seriesDaily = refreshDailySeries();
            }

            int endIndex = seriesDaily.getEndIndex(); // 這裡絕對不會是 -1 了
            strategy.onNewBar(seriesDaily, endIndex);
        }
    }

    /** * 防禦性添加 Bar，防止時間戳重複或時序倒流導致 TA4J 崩潰
     */
    private boolean addBarToSeries(Kline k) {
        // OKX 返回開盤時間，TA4J 需要收盤時間，此處 +15 分鐘對齊時間軸
        ZonedDateTime zonedEndTime = Instant.ofEpochMilli(k.getOpenTime())
                .atZone(ZoneId.of("UTC"))
                .plusMinutes(15);

        // 關鍵防禦：檢查新數據是否嚴格大於序列最後一根 Bar 的時間
        if (series15m.getBarCount() > 0) {
            ZonedDateTime lastEndTime = series15m.getLastBar().getEndTime();
            if (!zonedEndTime.isAfter(lastEndTime)) {
                log.debug("[{}] TA4J 攔截重複或過期 K線: 傳入={}, 序列尾部={}", symbol, zonedEndTime, lastEndTime);
                return false;
            }
        }

        Bar newBar = BaseBar.builder()
                .timePeriod(Duration.ofMinutes(15))
                .endTime(zonedEndTime)
                .openPrice(DecimalNum.valueOf(k.getOpen()))
                .highPrice(DecimalNum.valueOf(k.getHigh()))
                .lowPrice(DecimalNum.valueOf(k.getLow()))
                .closePrice(DecimalNum.valueOf(k.getClose()))
                .volume(DecimalNum.valueOf(k.getVolume()))
                .build();

        series15m.addBar(newBar);
        return true;
    }

    /**
     * 動態 15m 轉 日線（Daily）聚合器
     */
    public BarSeries getDailySeries() {
        if (seriesDaily == null) {
            seriesDaily = new BaseBarSeriesAggregator(
                    new DurationBarAggregator(Duration.ofDays(1), true))
                    .aggregate(series15m, symbol + "_daily");
        }
        return seriesDaily;
    }
}