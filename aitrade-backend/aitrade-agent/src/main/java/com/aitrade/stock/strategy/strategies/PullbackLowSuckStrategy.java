package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.enums.StrategyType;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 震盪攀升-趨勢動能量價共振低吸策略
 *
 * 核心設計理念：
 * 1. 趨勢通道：鎖定溫和多頭且低點逐步墊高的上升通道（WEAK_UPTREND）。
 * 2. 縮量回踩：尋找股價回踩 MA20/MA30 重要支撐位且成交量明顯萎縮（縮量洗盤）的機會。
 * 3. 動能超賣：結合 KDJ 低位金叉/勾頭、RSI 合理超賣區（35-52）與 MACD 零軸上方支撐。
 * 4. 籌碼過濾：要求獲利盤比例 >= 60%，確保整體趨勢健康而非空頭反彈。
 * 5. 診斷與評分：記錄所有過濾明細與低吸專屬綜合評分，支援回測覆盤與參數優化。
 */
@Component
public class PullbackLowSuckStrategy implements StockPickerStrategy {

    @Override
    public Integer getStrategyCode() {
        return StrategyType.PULLBACK_LOW_SUCK.getCode();
    }

    @Override
    public String getStrategyName() {
        return "震盪攀升-趨勢動能量價共振低吸策略";
    }

    @Override
    public boolean filter(List<TfKline> klines) {
        return filter(klines, null);
    }

    @Override
    public boolean filter(List<TfKline> klines, StockAnalysisContext context) {
        // 相容原有策略介面：內部調用完整分析方法，僅返回 passed 狀態
        StockPickDiagnostic diagnostic = analyze("UNKNOWN", "未知", LocalDate.now(), klines, context);
        return diagnostic.getPassed().equals(1);
    }

    /**
     * 核心增強分析方法：執行上升通道、縮量回踩支撐、動能修復檢查及多維度評分
     *
     * @param symbol      股票代碼
     * @param stockName   股票名稱
     * @param tradeDate   交易日期
     * @param klines      K線歷史數據列表（要求至少包含最近 120 個交易日）
     * @param context     盤口與籌碼分析上下文數據
     * @return            包含所有指標與得分的診斷記錄實體
     */
    @Override
    public StockPickDiagnostic analyze(String symbol, String stockName, LocalDate tradeDate,
                                       List<TfKline> klines, StockAnalysisContext context) {
        // 初始化診斷構建器，預設標記為未通過
        StockPickDiagnostic.StockPickDiagnosticBuilder builder = StockPickDiagnostic.builder()
                .symbol(symbol)
                .stockName(stockName)
                .tradeDate(tradeDate)
                .strategyCode(getStrategyCode())
                .strategyName(getStrategyName())
                .passed(0);

        // 資料防禦：若歷史 K 線數據不足 120 天，直接短路返回
        if (klines == null || klines.size() < 120) {
            return builder.failReason("KLINES_LESS_THAN_120").build();
        }

        int lastIdx = klines.size() - 1;
        TfKline today = klines.get(lastIdx);
        TfKline yesterday = klines.get(lastIdx - 1);

        // 記錄基本收盤價與當日漲跌幅
        builder.closePrice(today.getClose());
        BigDecimal changeRate = today.getClose().subtract(yesterday.getClose())
                .divide(yesterday.getClose(), 4, RoundingMode.HALF_UP);
        builder.changeRate(changeRate);

        // 計算 10 日均量，作為低吸縮量判定基準
        BigDecimal volMa10 = calculateVolMA(klines, lastIdx - 1, 10);
        BigDecimal volRatio = volMa10.compareTo(BigDecimal.ZERO) > 0 ?
                today.getVolume().divide(volMa10, 2, RoundingMode.HALF_UP) : BigDecimal.ONE;
        builder.volRatio(volRatio);

        // ==========================================
        // 1. 趨勢條件（Trend）：確認處於上升通道
        // ==========================================
        boolean matchChannel = checkWeakUptrendChannel(klines, lastIdx, builder);
        if (!matchChannel) {
            return builder.failReason("WEAK_UPTREND_CHANNEL_FAIL").build();
        }

        // ==========================================
        // 2. 量價條件（Volume-Price）：縮量回踩支撐位與止跌信號
        // ==========================================
        boolean matchPullback = checkVolumePricePullback(klines, lastIdx, volMa10, builder);
        if (!matchPullback) {
            return builder.failReason("VOLUME_PRICE_PULLBACK_FAIL").build();
        }

        // ==========================================
        // 3. 動能條件（Momentum）：超賣修復與低位金叉
        // ==========================================
        boolean matchMomentum = checkMomentumOversold(klines, lastIdx, builder);
        if (!matchMomentum) {
            return builder.failReason("MOMENTUM_OVERSOLD_FAIL").build();
        }

        // ==========================================
        // 4. 籌碼面與盤口特徵校驗（若有 Context 數據）
        // ==========================================
        if (context != null) {
            builder.profitRatio(context.getProfitRatio())
                    .isChipSinglePeak(context.getIsChipSinglePeak());

            // 低吸要求獲利盤 >= 60%，證明整體趨勢健康，非空頭反彈
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.60) {
                return builder.failReason("PROFIT_RATIO_LOW_FOR_LOW_SUCK").build();
            }
        }

        // ==========================================
        // 5. 完全通過過濾，執行低吸策略多維度綜合評分
        // ==========================================
        calculateAndSetLowSuckScores(builder, changeRate, volRatio, context, klines, lastIdx);

        // 最終標記為通過篩選
        return builder.passed(1)
                .failReason(null)
                .build();
    }

    // =========================================================================
    // 1. 趨勢驗證邏輯
    // =========================================================================

    /**
     * 驗證 WEAK_UPTREND 震盪攀升通道：MA20 > MA60 且斜率向上，且近 30 日低點逐步墊高。
     */
    private boolean checkWeakUptrendChannel(List<TfKline> klines, int endIdx,
                                            StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma60 = calculateMA(klines, endIdx, 60);

        BigDecimal prevMa20 = calculateMA(klines, endIdx - 5, 20);
        BigDecimal prevMa60 = calculateMA(klines, endIdx - 5, 60);

        // MA20 > MA60 且兩者斜率均為正（持續向上）
        boolean maAlignment = ma20.compareTo(ma60) > 0;
        boolean ma20SlopeUp = ma20.compareTo(prevMa20) > 0;
        boolean ma60SlopeUp = ma60.compareTo(prevMa60) > 0;

        if (!maAlignment || !ma20SlopeUp || !ma60SlopeUp) {
            return false;
        }

        // 檢查低點墊高：前 30 日最低價 > 30-60 日前的最低價
        BigDecimal minLowRecent = calculateMinLow(klines, endIdx - 1, 30);
        BigDecimal minLowPrior = calculateMinLow(klines, endIdx - 31, 30);

        // 記錄低點差額比率至 vcpAmplitude 欄位（複用存儲通道健康度）
        BigDecimal lowDiffRatio = minLowRecent.subtract(minLowPrior).divide(minLowPrior, 4, RoundingMode.HALF_UP);
        builder.vcpAmplitude(lowDiffRatio);

        return minLowRecent.compareTo(minLowPrior) > 0;
    }

    // =========================================================================
    // 2. 量價回踩與止跌邏輯
    // =========================================================================

    /**
     * 驗證縮量回踩與支撐：
     * 1. 回踩 MA20 或 MA30 支撐位（誤差 <= 1.5%）。
     * 2. 當日成交量縮小至 10 日均量的 75% 以下。
     * 3. 具備止跌信號（陽線收盤 或 長下影線）。
     */
    private boolean checkVolumePricePullback(List<TfKline> klines, int endIdx, BigDecimal volMa10,
                                             StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        TfKline today = klines.get(endIdx);

        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma30 = calculateMA(klines, endIdx, 30);

        // 1. 回踩支撐位：最低價或收盤價接近 MA20/MA30
        BigDecimal distToMa20 = today.getLow().subtract(ma20).abs().divide(ma20, 4, RoundingMode.HALF_UP);
        BigDecimal distToMa30 = today.getLow().subtract(ma30).abs().divide(ma30, 4, RoundingMode.HALF_UP);
        boolean touchSupport = distToMa20.doubleValue() <= 0.015 || distToMa30.doubleValue() <= 0.015;

        if (!touchSupport) return false;

        // 2. 縮量特徵：當日成交量 < 近 10 日均量的 75%
        boolean isShrinkVolume = today.getVolume().compareTo(volMa10.multiply(new BigDecimal("0.75"))) < 0;
        if (!isShrinkVolume) return false;

        // 3. 止跌信號：當日收陽線 或 帶有明顯下影線
        boolean isRedCandle = today.getClose().compareTo(today.getOpen()) >= 0;

        BigDecimal body = today.getClose().subtract(today.getOpen()).abs();
        BigDecimal lowerShadow = today.getOpen().min(today.getClose()).subtract(today.getLow());
        boolean hasLongLowerShadow = lowerShadow.compareTo(body.multiply(new BigDecimal("1.5"))) > 0;

        // 記錄形態標記
        builder.matchVcp(isRedCandle)         // 複用存儲紅K止跌
                .matchMaCoil(hasLongLowerShadow); // 複用存儲長下影線

        return isRedCandle || hasLongLowerShadow;
    }

    // =========================================================================
    // 3. 動能指標低位/超賣邏輯
    // =========================================================================

    /**
     * 驗證動能指標超賣與修復：
     * 1. KDJ 低位金叉或低位勾頭向上。
     * 2. RSI(14) 保持在 35 - 52 的多頭健康低吸區。
     * 3. MACD 保持在零軸上方或貼近零軸（水上加油）。
     */
    private boolean checkMomentumOversold(List<TfKline> klines, int endIdx,
                                          StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        // 1. KDJ 計算
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);

        double k = kdjToday[0];
        double d = kdjToday[1];
        double prevK = kdjYesterday[0];
        double prevD = kdjYesterday[1];

        builder.kdjK(BigDecimal.valueOf(k))
                .kdjD(BigDecimal.valueOf(d));

        boolean kdjCrossOrUp = (prevK <= prevD && k > d) || (k < 40 && k > prevK);

        // 2. RSI 計算 (RSI 14 在 35 ~ 52 之間)
        double rsi14 = calculateRSI(klines, endIdx, 14).doubleValue();
        builder.rsi14(BigDecimal.valueOf(rsi14));

        boolean rsiOversoldZone = rsi14 >= 35.0 && rsi14 <= 52.0;

        // 3. MACD 計算 (DIF/DEA 保持在 0 軸上方，或貼近 0 軸)
        double[] macdToday = calculateMACD(klines, endIdx);
        double dif = macdToday[0];
        double dea = macdToday[1];

        builder.macdDif(BigDecimal.valueOf(dif))
                .macdDea(BigDecimal.valueOf(dea))
                .macdHist(BigDecimal.valueOf(macdToday[2]));

        boolean macdZeroSupport = dif >= -0.02 && dea >= -0.02;

        return kdjCrossOrUp && rsiOversoldZone && macdZeroSupport;
    }

    // =========================================================================
    // 4. 低吸策略多維度綜合評分計算
    // =========================================================================

    /**
     * 計算震盪攀升低吸策略的綜合評分（趨勢通道健康度 35%、回踩縮量程度 30%、動能修復 20%、籌碼面 15%）
     */
    private void calculateAndSetLowSuckScores(StockPickDiagnostic.StockPickDiagnosticBuilder builder,
                                              BigDecimal changeRate, BigDecimal volRatio,
                                              StockAnalysisContext context, List<TfKline> klines, int endIdx) {
        // 1. 趨勢通道健康度得分 (35%)：依據 MA20 對 MA60 的乖離與向上斜率
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma60 = calculateMA(klines, endIdx, 60);
        BigDecimal trendStrength = ma20.subtract(ma60).divide(ma60, 4, RoundingMode.HALF_UP);
        BigDecimal trendScore = trendStrength.multiply(new BigDecimal("500"))
                .min(new BigDecimal("100")).max(new BigDecimal("60"));

        // 2. 回踩縮量得分 (30%)：縮量越厲害（如 volRatio 只有 0.4），說明洗盤越徹底，得分越高
        BigDecimal shrinkScore = new BigDecimal("1.0").subtract(volRatio)
                .multiply(new BigDecimal("100")).min(new BigDecimal("100")).max(new BigDecimal("50"));

        // 3. 動能修復得分 (20%)
        BigDecimal momentumScore = new BigDecimal("85.0");

        // 4. 籌碼面得分 (15%)
        BigDecimal chipScore = new BigDecimal("60.0");
        if (context != null && context.getProfitRatio() != null) {
            chipScore = context.getProfitRatio().multiply(new BigDecimal("100"));
            if (Boolean.TRUE.equals(context.getIsChipSinglePeak())) {
                chipScore = chipScore.add(new BigDecimal("10"));
            }
            chipScore = chipScore.min(new BigDecimal("100"));
        }

        // 加權綜合總得分計算
        BigDecimal totalScore = trendScore.multiply(new BigDecimal("0.35"))
                .add(shrinkScore.multiply(new BigDecimal("0.30")))
                .add(momentumScore.multiply(new BigDecimal("0.20")))
                .add(chipScore.multiply(new BigDecimal("0.15")));

        builder.volumeScore(shrinkScore)     // 複用存儲縮量得分
                .chipScore(chipScore)
                .momentumScore(momentumScore)
                .patternScore(trendScore)     // 複用存儲趨勢通道得分
                .totalScore(totalScore);
    }

    // =========================================================================
    // 底層技術指標與數學計算工具函數
    // =========================================================================

    private BigDecimal calculateMA(List<TfKline> klines, int endIdx, int window) {
        BigDecimal sum = BigDecimal.ZERO;
        int start = Math.max(0, endIdx - window + 1);
        int count = 0;
        for (int i = start; i <= endIdx; i++) {
            sum = sum.add(klines.get(i).getClose());
            count++;
        }
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVolMA(List<TfKline> klines, int endIdx, int window) {
        BigDecimal sum = BigDecimal.ZERO;
        int start = Math.max(0, endIdx - window + 1);
        int count = 0;
        for (int i = start; i <= endIdx; i++) {
            sum = sum.add(klines.get(i).getVolume());
            count++;
        }
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateMinLow(List<TfKline> klines, int endIdx, int window) {
        BigDecimal minLow = klines.get(endIdx).getLow();
        int start = Math.max(0, endIdx - window + 1);
        for (int i = start; i <= endIdx; i++) {
            if (klines.get(i).getLow().compareTo(minLow) < 0) minLow = klines.get(i).getLow();
        }
        return minLow;
    }

    private double[] calculateMACD(List<TfKline> klines, int endIdx) {
        double ema12 = klines.get(0).getClose().doubleValue();
        double ema26 = klines.get(0).getClose().doubleValue();
        double dea = 0.0;

        for (int i = 1; i <= endIdx; i++) {
            double close = klines.get(i).getClose().doubleValue();
            ema12 = ema12 * (11.0 / 13.0) + close * (2.0 / 13.0);
            ema26 = ema26 * (25.0 / 27.0) + close * (2.0 / 27.0);
            double dif = ema12 - ema26;
            dea = dea * (8.0 / 10.0) + dif * (2.0 / 10.0);

            if (i == endIdx) {
                double hist = (dif - dea) * 2.0;
                return new double[]{dif, dea, hist};
            }
        }
        return new double[]{0, 0, 0};
    }

    private double[] calculateKDJ(List<TfKline> klines, int endIdx, int n, int m1, int m2) {
        double k = 50.0;
        double d = 50.0;

        int startIdx = Math.max(0, endIdx - 60);
        for (int i = startIdx; i <= endIdx; i++) {
            int windowStart = Math.max(0, i - n + 1);
            double lowN = klines.get(windowStart).getLow().doubleValue();
            double highN = klines.get(windowStart).getHigh().doubleValue();

            for (int j = windowStart; j <= i; j++) {
                lowN = Math.min(lowN, klines.get(j).getLow().doubleValue());
                highN = Math.max(highN, klines.get(j).getHigh().doubleValue());
            }

            double close = klines.get(i).getClose().doubleValue();
            double rsv = (highN == lowN) ? 50.0 : (close - lowN) / (highN - lowN) * 100.0;

            k = (2.0 * k + rsv) / 3.0;
            d = (2.0 * d + k) / 3.0;
        }
        return new double[]{k, d, 3.0 * k - 2.0 * d};
    }

    private BigDecimal calculateRSI(List<TfKline> klines, int endIdx, int period) {
        BigDecimal gainSum = BigDecimal.ZERO;
        BigDecimal lossSum = BigDecimal.ZERO;

        int start = Math.max(1, endIdx - period + 1);
        for (int i = start; i <= endIdx; i++) {
            BigDecimal change = klines.get(i).getClose().subtract(klines.get(i - 1).getClose());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gainSum = gainSum.add(change);
            } else {
                lossSum = lossSum.add(change.abs());
            }
        }

        if (lossSum.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100");
        if (gainSum.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal avgGain = gainSum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
        BigDecimal avgLoss = lossSum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
        BigDecimal rs = avgGain.divide(avgLoss, 6, RoundingMode.HALF_UP);

        BigDecimal hundred = new BigDecimal("100");
        return hundred.subtract(hundred.divide(BigDecimal.ONE.add(rs), 6, RoundingMode.HALF_UP));
    }
}