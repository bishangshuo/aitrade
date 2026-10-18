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
 * 超跌反彈-趨勢動能量價共振反彈策略
 *
 * 核心設計理念：
 * 1. 趨勢與乖離率：鎖定空頭排列下的短期強烈負乖離（BIAS20 < -8%），存在強烈的均線修復拉力。
 * 2. 極端超賣：結合 RSI、KDJ 低位超賣與 MACD 綠柱縮短，確認空頭動能衰竭。
 * 3. 量價止跌：尋找長下影線（金針探底）或強勢反包形態，並伴隨成交量放大（抄底資金介入）。
 * 4. 籌碼過濾：結合獲利盤比例，避免套牢盤過重拖累反彈。
 * 5. 診斷與評分：記錄所有過濾明細與超跌反彈專屬評分，支援回測覆盤與參數優化。
 */
@Component
public class OversoldReboundStrategy implements StockPickerStrategy {

    @Override
    public Integer getStrategyCode() {
        return StrategyType.OVERSOLD_REBOUND.getCode();
    }

    @Override
    public String getStrategyName() {
        return "超跌反彈-趨勢動能量價共振反彈策略";
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
     * 核心增強分析方法：執行超跌乖離、極端超賣、止跌量價檢查及多維度評分
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

        // 計算 5 日均量，作為超跌反彈放量判定基準
        BigDecimal volMa5 = calculateVolMA(klines, lastIdx - 1, 5);
        BigDecimal volRatio = volMa5.compareTo(BigDecimal.ZERO) > 0 ?
                today.getVolume().divide(volMa5, 2, RoundingMode.HALF_UP) : BigDecimal.ONE;
        builder.volRatio(volRatio);

        // ==========================================
        // 1. 趨勢與乖離率條件（Trend & Bias）
        // ==========================================
        boolean matchBias = checkDowntrendOversoldBias(klines, lastIdx, builder);
        if (!matchBias) {
            return builder.failReason("BIAS_NOT_ENOUGH_OR_NOT_DOWNTREND").build();
        }

        // ==========================================
        // 2. 動能條件（Momentum）：極端超賣與衰竭修復
        // ==========================================
        boolean matchMomentum = checkOversoldMomentum(klines, lastIdx, builder);
        if (!matchMomentum) {
            return builder.failReason("OVERSOLD_MOMENTUM_FAIL").build();
        }

        // ==========================================
        // 3. 量價條件（Volume-Price）：止跌反轉形態與資金流入
        // ==========================================
        boolean matchReversal = checkVolumePriceReversal(klines, lastIdx, changeRate, volMa5, builder);
        if (!matchReversal) {
            return builder.failReason("VOLUME_PRICE_REVERSAL_FAIL").build();
        }

        // ==========================================
        // 4. 籌碼面與盤口特徵校驗（若有 Context 數據）
        // ==========================================
        if (context != null) {
            builder.profitRatio(context.getProfitRatio())
                    .isChipSinglePeak(context.getIsChipSinglePeak());

            // 獲利盤比例低於 60% 排除（超跌反彈可適當放寬至 60%，此處維持 70% 嚴格過濾或按原策略）
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.70) {
                return builder.failReason("PROFIT_RATIO_LOW").build();
            }
        }

        // ==========================================
        // 5. 完全通過過濾，執行超跌反彈多維度綜合評分
        // ==========================================
        calculateAndSetOversoldScores(builder, changeRate, volRatio, context, klines, lastIdx);

        // 最終標記為通過篩選
        return builder.passed(1)
                .failReason(null)
                .build();
    }

    // =========================================================================
    // 1. 趨勢與乖離率（Bias）驗證邏輯
    // =========================================================================

    /**
     * 驗證跌勢超跌偏離度：MA20 < MA60（空頭排列），且 20 日乖離率 (BIAS20) <= -8%。
     */
    private boolean checkDowntrendOversoldBias(List<TfKline> klines, int endIdx,
                                               StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        TfKline today = klines.get(endIdx);

        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma60 = calculateMA(klines, endIdx, 60);

        // 中短期均線呈空頭架構
        if (ma20.compareTo(ma60) >= 0) {
            return false;
        }

        // BIAS20 = (Close - MA20) / MA20
        BigDecimal bias20 = today.getClose().subtract(ma20).divide(ma20, 4, RoundingMode.HALF_UP);

        // 記錄乖離率至診斷實體的 vcpAmplitude 欄位（複用存儲數值）
        builder.vcpAmplitude(bias20);

        // 負乖離率需低於或等於 -8%
        return bias20.doubleValue() <= -0.08;
    }

    // =========================================================================
    // 2. 動能極端超賣驗證邏輯
    // =========================================================================

    /**
     * 驗證動能指標超賣與衰竭修復：
     * 1. RSI(6) < 28 或 RSI(14) < 32。
     * 2. KDJ 的 K 值 < 25 且勾頭向上。
     * 3. MACD 綠柱（HIST）較前一日縮短。
     */
    private boolean checkOversoldMomentum(List<TfKline> klines, int endIdx,
                                          StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        // 1. RSI 6 和 14 計算
        double rsi6 = calculateRSI(klines, endIdx, 6).doubleValue();
        double rsi14 = calculateRSI(klines, endIdx, 14).doubleValue();

        // 記錄 RSI 數值
        builder.rsi14(BigDecimal.valueOf(rsi14));

        boolean isRsiOversold = rsi6 < 28.0 || rsi14 < 32.0;
        if (!isRsiOversold) return false;

        // 2. KDJ 計算 (K 值在低位 25 以下，且 K > PrevK 勾頭向上)
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);

        builder.kdjK(BigDecimal.valueOf(kdjToday[0]))
                .kdjD(BigDecimal.valueOf(kdjToday[1]));

        boolean isKdjLowHook = kdjYesterday[0] < 25.0 && kdjToday[0] > kdjYesterday[0];

        // 3. MACD 綠柱縮短 (Hist < 0 且 Hist > PrevHist)
        double[] macdToday = calculateMACD(klines, endIdx);
        double[] macdYesterday = calculateMACD(klines, endIdx - 1);

        builder.macdDif(BigDecimal.valueOf(macdToday[0]))
                .macdDea(BigDecimal.valueOf(macdToday[1]))
                .macdHist(BigDecimal.valueOf(macdToday[2]));

        boolean isMacdGreenShortening = macdToday[2] < 0 && macdToday[2] > macdYesterday[2];

        return isKdjLowHook && isMacdGreenShortening;
    }

    // =========================================================================
    // 3. 量價止跌與抄底反轉邏輯
    // =========================================================================

    /**
     * 驗證量價止跌反轉形態：
     * 1. 放量驗證：成交量 >= 1.3 * Vol_MA5。
     * 2. K 線形態：錘頭線（下影線 >= 2倍實體） 或 陽包陰（收盤超過昨日陰線實體一半）。
     * 3. 漲幅 >= 2.0%。
     */
    private boolean checkVolumePriceReversal(List<TfKline> klines, int endIdx,
                                             BigDecimal changeRate, BigDecimal volMa5,
                                             StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 1. 放量驗證：抄底資金介入，成交量 >= 1.3 * Vol_MA5
        if (today.getVolume().compareTo(volMa5.multiply(new BigDecimal("1.3"))) < 0) {
            return false;
        }

        // 2. K 線形態判定
        BigDecimal body = today.getClose().subtract(today.getOpen()).abs();
        BigDecimal lowerShadow = today.getOpen().min(today.getClose()).subtract(today.getLow());
        boolean isHammerLine = lowerShadow.compareTo(body.multiply(new BigDecimal("2.0"))) > 0;

        boolean yesterdayIsYin = yesterday.getClose().compareTo(yesterday.getOpen()) < 0;
        BigDecimal yesterdayMidPoint = yesterday.getOpen().add(yesterday.getClose()).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        boolean isSunWrapYin = today.getClose().compareTo(today.getOpen()) > 0
                && yesterdayIsYin
                && today.getClose().compareTo(yesterdayMidPoint) > 0;

        // 標記具體觸發形態
        builder.matchVcp(isHammerLine)       // 複用存儲錘頭線標記
                .matchMaCoil(isSunWrapYin);    // 複用存儲陽包陰標記

        // 漲幅 >= 2.0%
        boolean strongBounce = changeRate.doubleValue() >= 0.02;

        return (isHammerLine || isSunWrapYin) && strongBounce;
    }

    // =========================================================================
    // 4. 超跌反彈策略多維度綜合評分計算
    // =========================================================================

    /**
     * 計算超跌反彈策略的綜合評分（乖離超跌深度 35%、量價反轉力度 30%、動能修復 20%、籌碼面 15%）
     */
    private void calculateAndSetOversoldScores(StockPickDiagnostic.StockPickDiagnosticBuilder builder,
                                               BigDecimal changeRate, BigDecimal volRatio,
                                               StockAnalysisContext context, List<TfKline> klines, int endIdx) {
        // 1. 超跌乖離深度得分 (35%)：負乖離越大（如 -15%），超跌得分越高
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal bias20 = klines.get(endIdx).getClose().subtract(ma20).divide(ma20, 4, RoundingMode.HALF_UP);
        // 假設負乖離從 -8% 到 -20% 對應 60 到 100 分
        BigDecimal biasScore = bias20.abs().multiply(new BigDecimal("350"))
                .min(new BigDecimal("100")).max(new BigDecimal("50"));

        // 2. 量價反轉力度得分 (30%)：漲幅與放量倍數
        BigDecimal priceScore = changeRate.multiply(new BigDecimal("1000")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volScore = volRatio.multiply(new BigDecimal("30.0")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volumePowerScore = priceScore.multiply(new BigDecimal("0.5")).add(volScore.multiply(new BigDecimal("0.5")));

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
        BigDecimal totalScore = biasScore.multiply(new BigDecimal("0.35"))
                .add(volumePowerScore.multiply(new BigDecimal("0.30")))
                .add(momentumScore.multiply(new BigDecimal("0.20")))
                .add(chipScore.multiply(new BigDecimal("0.15")));

        builder.volumeScore(volumePowerScore)
                .chipScore(chipScore)
                .momentumScore(momentumScore)
                .patternScore(biasScore) // 複用 patternScore 欄位存儲超跌乖離得分
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