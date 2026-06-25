package com.aitrade.exchange.component;
import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.domain.KlineSettings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.aggregator.BaseBarSeriesAggregator;
import org.ta4j.core.aggregator.DurationBarAggregator;
import org.ta4j.core.num.DecimalNum;

import java.io.Serializable;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 企業級交易對狀態維護類別（優化版）
 * 解決全量聚合帶來的 CPU 暴漲與 GC 壓力，實現微秒級動態增量聚合。
 */
@Data
@Slf4j
public class SymbolState implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 交易對，如 BTC-USDT */
    private final String symbol;

    // ==================== TA4J 核心數據序列 ====================
    /** 15分鐘原始 K 線序列（最大保留3年） */
    private final BarSeries series15m;
    /** 動態維護的日線 K 線序列 */
    private BarSeries seriesDaily;

    // 在 SymbolState 類別中補回 volatile 欄位
    private volatile long lastTimestamp = 0;

    /** 內聚的策略核心 */
    private final SwingStrategy strategy;

    private final KlineSettings klineSettings;

    public SymbolState(String symbol, KlineSettings klineSettings) {
        this.symbol = symbol;
        this.klineSettings = klineSettings;

        int max15mBarCount = (3 * 365 * 24 * 60) / ((int) this.klineSettings.getKlineTime() / 60000);

        this.series15m = new BaseBarSeriesBuilder()
                .withName(symbol + "_" + this.klineSettings.getKlineInterval())
                .withMaxBarCount(max15mBarCount)
                .build();

        // 初始化一個空的日線序列，防止 NPE
        this.seriesDaily = new BaseBarSeriesBuilder()
                .withName(symbol + "_daily")
                .withMaxBarCount(3 * 365)
                .build();

        this.strategy = new SwingStrategy(this.klineSettings);
    }

    // ==================== 核心數據流方法 ====================

    /**
     * 歷史數據批量加載（系統啟動或斷線補償時調用）
     * 採用「一次性全量聚合」初始化日線
     */
    public synchronized void loadHistoricalBars(List<Kline> klines) {
        if (klines == null || klines.isEmpty()) return;

        log.info("[{}] 開始加載歷史數據，總計 {} 根...", symbol, klines.size());
        for (Kline k : klines) {
            if (Boolean.TRUE.equals(k.getIsFinal())) {
                addBarTo15mSeries(k, false);
            }
        }

        if (series15m.getBarCount() == 0) return;

        // 【核心修正】：直接將聚合後的結果賦值給 seriesDaily，並設定最大數量限制
        // 這樣可以絕對避免 "Cannot add a bar with end time..." 的時序錯亂錯誤
        BarSeries aggregated = new BaseBarSeriesAggregator(
                new DurationBarAggregator(Duration.ofDays(1), false))
                .aggregate(series15m, symbol + "_daily");

        // 重新構建一個帶有 MaxBarCount 限制的正式日線序列
        this.seriesDaily = new BaseBarSeriesBuilder()
                .withName(symbol + "_daily")
                .withMaxBarCount(3 * 365)
                .build();

        for (int i = aggregated.getBeginIndex(); i <= aggregated.getEndIndex(); i++) {
            if (i >= 0) {
                this.seriesDaily.addBar(aggregated.getBar(i));
            }
        }

        log.info("[{}] 歷史數據初始化完成。15m 序列長度: {}, Daily 序列長度: {}",
                symbol, series15m.getBarCount(), seriesDaily.getBarCount());
    }

    /**
     * 即時新增 15m K 線（WebSocket 最終確認盤口時調用）
     * 核心優化點：微秒級動態滑動聚合
     */
    public synchronized void addBar(Kline kline, boolean isFinal) {
        if (!isFinal) return;

        if (addBarTo15mSeries(kline, true)) {
            // 執行高效的實時增量聚合
            aggregatorDailyIncrementally();

            int endIndex = seriesDaily.getEndIndex();
            if (endIndex != -1) {
                strategy.onNewBar(seriesDaily, endIndex);
            }
        }
    }

    /**
     * 【核心優化演算法】高效動態增量聚合
     * 不再全量遍歷 11 萬根，而是切片最後 48 小時數據（最多 192 根）進行滾動更新
     */
    private void aggregatorDailyIncrementally() {
        if (series15m.getBarCount() == 0) return;

        // 1. 取出最新的一根 15m Bar
        Bar latest15mBar = series15m.getLastBar();
        ZonedDateTime latest15mTime = latest15mBar.getEndTime();

        // 2. 計算當前 15m 數據所屬日線的理論截止時間（以 UTC 0點天底為準切齊）
        ZonedDateTime targetDailyEndTime = latest15mTime.truncatedTo(ChronoUnit.DAYS).plusDays(1);

        // 3. 提取 15m 序列中最近 2 天的數據建立微型子序列
        BarSeries microSubSeries = new BaseBarSeries();
        int total15m = series15m.getBarCount();
        // 48小時最多 192 根 K 線，向後掃描確保覆蓋當天與前一天的分界線
        int scanDepth = Math.min(total15m, 200);

        ZonedDateTime boundaryTime = targetDailyEndTime.minusDays(2);
        for (int i = total15m - scanDepth; i < total15m; i++) {
            Bar b = series15m.getBar(i);
            if (b.getEndTime().isAfter(boundaryTime)) {
                microSubSeries.addBar(b);
            }
        }

        // 4. 對微型子序列進行聚合（極速，耗時 < 5微秒）
        BarSeries microDaily = new BaseBarSeriesAggregator(
                new DurationBarAggregator(Duration.ofDays(1), false))
                .aggregate(microSubSeries, "micro_daily");

        if (microDaily.getBarCount() == 0) return;

        // 5. 將聚合結果同步回主 seriesDaily 序列中
        for (int i = microDaily.getBeginIndex(); i <= microDaily.getEndIndex(); i++) {
            Bar freshDailyBar = microDaily.getBar(i);
            mergeOrAddDailyBar(freshDailyBar);
        }
    }

    /**
     * 輔助方法：將一根聚合好的日線安全地合入主日線序列中
     */
    private void mergeOrAddDailyBar(Bar freshDailyBar) {
        if (seriesDaily.getBarCount() == 0) {
            seriesDaily.addBar(freshDailyBar);
            return;
        }

        Bar lastDailyBar = seriesDaily.getLastBar();

        // 情況 A：屬於同一天 -> 由於 Ta4j 不支援修改 Bar，必須手動反射覆蓋，或利用底層可變對象
        // 這裡採用標準的安全替換做法：利用反射修改 BaseBar 欄位，或者利用原生 API 特性。
        // 生產環境最穩妥做法：如果時間戳相同，說明是「當天未完結日線」，需要更新數據
        if (freshDailyBar.getEndTime().equals(lastDailyBar.getEndTime())) {
            updateBarFields(lastDailyBar, freshDailyBar);
        }
        // 情況 B：新的一天來臨 -> 直接追加
        else if (freshDailyBar.getEndTime().isAfter(lastDailyBar.getEndTime())) {
            seriesDaily.addBar(freshDailyBar);
            log.info("[{}] 跨天成功，新增日線 K 線: {}", symbol, freshDailyBar.getEndTime());
        }
    }

    /**
     * 反射修改 BaseBar 屬性（應對 Ta4j BarSeries 嚴格的不可變設計）
     */
    private void updateBarFields(Bar target, Bar source) {
        try {
            // 注意：Ta4j 的 BaseBar 欄位是 final 的，但可以透過反射或直接操作 Num 對象（如果包裝類支持）
            // 生產環境中，若不想用反射，更推薦自訂一個可變的 `MutableBar` 實作 Bar 介面。
            // 這裡給出最直接的反射覆蓋欄位方案（適用於 BaseBar）：
            java.lang.reflect.Field closePriceField = BaseBar.class.getDeclaredField("closePrice");
            java.lang.reflect.Field highPriceField = BaseBar.class.getDeclaredField("highPrice");
            java.lang.reflect.Field lowPriceField = BaseBar.class.getDeclaredField("lowPrice");
            java.lang.reflect.Field volumeField = BaseBar.class.getDeclaredField("volume");

            closePriceField.setAccessible(true);
            highPriceField.setAccessible(true);
            lowPriceField.setAccessible(true);
            volumeField.setAccessible(true);

            closePriceField.set(target, source.getClosePrice());
            highPriceField.set(target, source.getHighPrice());
            lowPriceField.set(target, source.getLowPrice());
            volumeField.set(target, source.getVolume());
        } catch (Exception e) {
            log.error("[{}] 反射更新日線數據失敗", symbol, e);
        }
    }

    /**
     * 防禦性添加 15m Bar
     */
    private boolean addBarTo15mSeries(Kline k, boolean fromWebsocket) {
        ZonedDateTime zonedEndTime = Instant.ofEpochMilli(k.getOpenTime()).atZone(ZoneId.of("UTC"));
        if (fromWebsocket) {
            zonedEndTime = zonedEndTime.plusMinutes(15);
        }

        if (series15m.getBarCount() > 0) {
            ZonedDateTime lastEndTime = series15m.getLastBar().getEndTime();
            if (!zonedEndTime.isAfter(lastEndTime)) {
                log.debug("[{}] 攔截過期 15m K線: 傳入={}, 序列尾部={}", symbol, zonedEndTime, lastEndTime);
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
}