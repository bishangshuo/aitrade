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
 * 底部反轉-趨勢動能量價共振突破策略
 *
 * 核心設計理念：
 * 1. 趨勢確認：鎖定近 120 日低位區間（超跌區），且中短期均線（MA20）止跌走平。
 * 2. 量價起爆：尋找底部經地量整理後的大陽線放量突破（放量 2 倍以上，一陽穿多線）。
 * 3. 動能反轉：通過 MACD 底背離或 KDJ 低位金瘡與 RSI 向上突破確認底部反轉動能。
 * 4. 籌碼過濾：結合獲利盤比例，避免套牢盤過重。
 * 5. 診斷與評分：記錄所有過濾明細與多維度評分，支援回測覆盤與參數優化。
 */
@Component
public class BottomBreakoutStrategy implements StockPickerStrategy {

    @Override
    public Integer getStrategyCode() {
        return StrategyType.BOTTOM_BREAKOUT.getCode();
    }
    @Override
    public String getStrategyName() {
        return "底部反轉-趨勢動能量價共振突破策略";
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
     * 核心增強分析方法：執行底部趨勢、量價突破、動能背離檢查及多維度評分
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

        // 計算 20 日均量，作為底部放量判定基準
        BigDecimal volMa20 = calculateVolMA(klines, lastIdx - 1, 20);
        BigDecimal volRatio = volMa20.compareTo(BigDecimal.ZERO) > 0 ?
                today.getVolume().divide(volMa20, 2, RoundingMode.HALF_UP) : BigDecimal.ONE;
        builder.volRatio(volRatio);

        // ==========================================
        // 1. 趨勢條件（Trend）：確認處於底部區域且均線止跌走平
        // ==========================================
        boolean matchTrend = checkBottomBaseTrend(klines, lastIdx, builder);
        if (!matchTrend) {
            return builder.failReason("TREND_NOT_BOTTOM").build();
        }

        // ==========================================
        // 2. 量價條件（Volume-Price）：地量後的大陽線倍量突破
        // ==========================================
        boolean matchVolumePrice = checkBottomVolumePriceBreakout(klines, lastIdx, changeRate, volMa20);
        if (!matchVolumePrice) {
            return builder.failReason("VOLUME_PRICE_BREAKOUT_FAIL").build();
        }

        // ==========================================
        // 3. 動能條件（Momentum）：MACD 底背離 或 KDJ/RSI 低位反轉
        // ==========================================
        boolean matchMomentum = checkBottomMomentum(klines, lastIdx);
        if (!matchMomentum) {
            return builder.failReason("MOMENTUM_CONDITION_FAIL").build();
        }

        // ==========================================
        // 4. 籌碼面與盤口特徵校驗（若有 Context 數據）
        // ==========================================
        if (context != null) {
            builder.profitRatio(context.getProfitRatio())
                    .isChipSinglePeak(context.getIsChipSinglePeak());

            // 獲利盤比例低於 70% 排除，避免上方套牢盤沉重
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.70) {
                return builder.failReason("PROFIT_RATIO_LOW").build();
            }
        }

        // ==========================================
        // 5. 完全通過過濾，執行多維度綜合評分
        // ==========================================
        calculateAndSetBottomScores(builder, changeRate, volRatio, context, klines, lastIdx);

        // 最終標記為通過篩選
        return builder.passed(1)
                .failReason(null)
                .build();
    }

    // =========================================================================
    // 1. 趨勢驗證邏輯
    // =========================================================================

    /**
     * 驗證底部區域與止跌：當前價格處於近 120 日低位區間 30% 以內，且 MA20 止跌走平。
     */
    private boolean checkBottomBaseTrend(List<TfKline> klines, int endIdx,
                                         StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        TfKline today = klines.get(endIdx);

        // 計算近 120 日最高點與最低點
        BigDecimal maxHigh120 = calculateMaxHigh(klines, endIdx, 120);
        BigDecimal minLow120 = calculateMinLow(klines, endIdx, 120);

        BigDecimal range = maxHigh120.subtract(minLow120);
        if (range.compareTo(BigDecimal.ZERO) == 0) return false;

        // 計算當前價格在近 120 日的相對位置分位數
        BigDecimal positionRatio = today.getClose().subtract(minLow120).divide(range, 4, RoundingMode.HALF_UP);

        // 記錄 VCP/底部振幅相關指標至診斷實體
        builder.vcpAmplitude(positionRatio);

        // 若分位數大於 30%，說明不在底部區域
        if (positionRatio.doubleValue() > 0.30) {
            return false;
        }

        // MA20 止跌走平：當前 MA20 >= 5 日前的 MA20 * 0.995
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal prevMa20 = calculateMA(klines, endIdx - 5, 20);

        return ma20.compareTo(prevMa20.multiply(new BigDecimal("0.995"))) >= 0;
    }

    // =========================================================================
    // 2. 量價突破邏輯
    // =========================================================================

    /**
     * 驗證底部倍量突破：漲幅 >= 4%，成交量 >= 2.0 倍 20 日均量，收盤站上 MA20 與 MA60。
     */
    private boolean checkBottomVolumePriceBreakout(List<TfKline> klines, int endIdx,
                                                   BigDecimal changeRate, BigDecimal volMa20) {
        // 1. 當日漲幅 >= 4%
        if (changeRate.doubleValue() < 0.04) return false;

        // 2. 放量 >= 2.0 * Vol_MA20
        TfKline today = klines.get(endIdx);
        if (today.getVolume().compareTo(volMa20.multiply(new BigDecimal("2.0"))) < 0) {
            return false;
        }

        // 3. 一陽穿多線：收盤價站上 MA20 和 MA60
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma60 = calculateMA(klines, endIdx, 60);

        return today.getClose().compareTo(ma20) > 0 && today.getClose().compareTo(ma60) > 0;
    }

    // =========================================================================
    // 3. 動能指標底背離 / 反轉邏輯
    // =========================================================================

    /**
     * 驗證動能反轉（滿足其一即可）：
     * 情況 A: MACD 底背離。
     * 情況 B: KDJ 超賣區（K < 35）低位金叉，且 RSI(14) 從低位向上突破。
     */
    private boolean checkBottomMomentum(List<TfKline> klines, int endIdx) {
        // 1. KDJ 計算與低位金叉確認
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);
        boolean kdjBottomCross = kdjYesterday[0] <= kdjYesterday[1]
                && kdjToday[0] > kdjToday[1]
                && kdjYesterday[0] < 35.0;

        // 2. RSI 計算與低位向上穿越確認
        double rsiToday = calculateRSI(klines, endIdx, 14).doubleValue();
        double rsiYesterday = calculateRSI(klines, endIdx - 1, 14).doubleValue();
        boolean rsiBreakout = rsiYesterday < 45.0 && rsiToday >= 48.0;

        // 3. MACD 底背離檢測
        boolean macdDivergence = checkMacdBottomDivergence(klines, endIdx);

        // 動能要求：滿足 MACD 底背離，或者（KDJ 低位金叉 + RSI 低位突破）
        return macdDivergence || (kdjBottomCross && rsiBreakout);
    }

    /**
     * 檢測 MACD 底背離
     */
    private boolean checkMacdBottomDivergence(List<TfKline> klines, int endIdx) {
        double[] macdToday = calculateMACD(klines, endIdx);
        double[] macdPrior = calculateMACD(klines, endIdx - 20);

        // 當前價格 <= 20 日前的價格（股價走低/新低）
        boolean priceLower = klines.get(endIdx).getClose().compareTo(klines.get(endIdx - 20).getClose()) <= 0;
        // 當前 DIF > 20 日前的 DIF（指標抬升，形成背離）
        boolean difHigher = macdToday[0] > macdPrior[0];

        return priceLower && difHigher;
    }

    // =========================================================================
    // 4. 底部策略多維度綜合評分計算
    // =========================================================================

    /**
     * 計算底部反轉策略的綜合評分（趨勢深度 30%、量價爆發 30%、動能反轉 20%、籌碼面 20%）
     */
    private void calculateAndSetBottomScores(StockPickDiagnostic.StockPickDiagnosticBuilder builder,
                                             BigDecimal changeRate, BigDecimal volRatio,
                                             StockAnalysisContext context, List<TfKline> klines, int endIdx) {
        // 1. 趨勢深度得分 (30%)：位置越接近 120 日底部越給高分
        BigDecimal minLow120 = calculateMinLow(klines, endIdx, 120);
        BigDecimal maxHigh120 = calculateMaxHigh(klines, endIdx, 120);
        BigDecimal range = maxHigh120.subtract(minLow120);
        BigDecimal positionRatio = range.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("0.15") :
                klines.get(endIdx).getClose().subtract(minLow120).divide(range, 4, RoundingMode.HALF_UP);
        // 位置越低（如 0.05），趨勢底部得分越高
        BigDecimal trendScore = BigDecimal.ONE.subtract(positionRatio).multiply(new BigDecimal("100"))
                .min(new BigDecimal("100")).max(BigDecimal.ZERO);

        // 2. 量價爆發力得分 (30%)：漲幅與放量倍數
        BigDecimal priceScore = changeRate.multiply(new BigDecimal("1000")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volScore = volRatio.multiply(new BigDecimal("30.0")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volumePowerScore = priceScore.multiply(new BigDecimal("0.5")).add(volScore.multiply(new BigDecimal("0.5")));

        // 3. 動能反轉得分 (20%)
        BigDecimal momentumScore = new BigDecimal("85.0");

        // 4. 籌碼面得分 (20%)
        BigDecimal chipScore = new BigDecimal("60.0");
        if (context != null && context.getProfitRatio() != null) {
            chipScore = context.getProfitRatio().multiply(new BigDecimal("100"));
            if (Boolean.TRUE.equals(context.getIsChipSinglePeak())) {
                chipScore = chipScore.add(new BigDecimal("10"));
            }
            chipScore = chipScore.min(new BigDecimal("100"));
        }

        // 加權綜合總得分計算
        BigDecimal totalScore = trendScore.multiply(new BigDecimal("0.30"))
                .add(volumePowerScore.multiply(new BigDecimal("0.30")))
                .add(momentumScore.multiply(new BigDecimal("0.20")))
                .add(chipScore.multiply(new BigDecimal("0.20")));

        builder.volumeScore(volumePowerScore)
                .chipScore(chipScore)
                .momentumScore(momentumScore)
                .patternScore(trendScore) // 複用 patternScore 欄位存儲趨勢得分
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

    private BigDecimal calculateMaxHigh(List<TfKline> klines, int endIdx, int window) {
        BigDecimal maxHigh = klines.get(endIdx).getHigh();
        int start = Math.max(0, endIdx - window + 1);
        for (int i = start; i <= endIdx; i++) {
            if (klines.get(i).getHigh().compareTo(maxHigh) > 0) maxHigh = klines.get(i).getHigh();
        }
        return maxHigh;
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