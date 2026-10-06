package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 底部轉折（BOTTOM_REVERSAL）環境下的「趨勢-動能-量價」共振反轉突破策略
 */
@Component
public class BottomBreakoutStrategy implements StockPickerStrategy {

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
        if (klines == null || klines.size() < 120) return false;

        int lastIdx = klines.size() - 1;

        // ==========================================
        // 1. 趨勢條件（Trend）：確認處於底部區域，且下行趨勢止跌走平
        // ==========================================
        if (!checkBottomBaseTrend(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 2. 量價條件（Volume-Price）：地量後的大陽線倍量突破
        // ==========================================
        if (!checkBottomVolumePriceBreakout(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 3. 動能條件（Momentum）：MACD 底背離 / KDJ 低位金叉
        // ==========================================
        if (!checkBottomMomentum(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 4. 籌碼面與盤口輔助（可選 Context）
        // ==========================================
        if (context != null) {
            // 底部反轉形態下，若籌碼開始在底部單峰密集，獲利盤迅速提升（例如 > 50%），代表洗盤結束
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.40) {
                return false;
            }
        }

        return true;
    }

    // =========================================================================
    // 1. 趨勢驗證邏輯
    // =========================================================================

    /**
     * 驗證底部區域與止跌：
     * - 前期經歷顯著下跌或長時間低位橫盤（當前價格處於近 120 日低位區間 20% 以內）。
     * - 短期均線走平（MA20 斜率轉正或止跌）。
     */
    private boolean checkBottomBaseTrend(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);

        // 計算近 120 日最高點與最低點
        BigDecimal maxHigh120 = calculateMaxHigh(klines, endIdx, 120);
        BigDecimal minLow120 = calculateMinLow(klines, endIdx, 120);

        BigDecimal range = maxHigh120.subtract(minLow120);
        if (range.compareTo(BigDecimal.ZERO) == 0) return false;

        // 當前價格在近 120 日相對低位區域（分位數 < 30%）
        BigDecimal positionRatio = today.getClose().subtract(minLow120).divide(range, 4, RoundingMode.HALF_UP);
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
     * 驗證底部倍量突破：
     * - 當日大陽線突破（漲幅 >= 4%）。
     * - 成交量顯著放大（當日成交量 >= 近 20 日平均成交量的 2.0 倍，即「地量起爆」）。
     * - 突破關鍵壓力：收盤價站上 MA20 與 MA60。
     */
    private boolean checkBottomVolumePriceBreakout(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 1. 當日漲幅 >= 4%
        BigDecimal changeRate = today.getClose().subtract(yesterday.getClose())
                .divide(yesterday.getClose(), 4, RoundingMode.HALF_UP);
        if (changeRate.doubleValue() < 0.04) return false;

        // 2. 放量 >= 2.0 * MA20_Vol
        BigDecimal volMa20 = calculateVolMA(klines, endIdx - 1, 20);
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
     * 驗證動能反轉（满足其一即可）：
     * - 情況 A: MACD 底背離（股價創近 40 日新低，但 DIF 未創新低，且今日 MACD 金叉或紅柱放大）。
     * - 情況 B: KDJ 超賣區（K < 30）強勢金叉，且 RSI(14) 從低位（< 40）向上突破 50。
     */
    private boolean checkBottomMomentum(List<TfKline> klines, int endIdx) {
        // 1. KDJ 計算
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);
        boolean kdjBottomCross = kdjYesterday[0] <= kdjYesterday[1]
                && kdjToday[0] > kdjToday[1]
                && kdjYesterday[0] < 35.0; // 在低位發生的金叉

        // 2. RSI 計算 (低位向上穿越)
        double rsiToday = calculateRSI(klines, endIdx, 14).doubleValue();
        double rsiYesterday = calculateRSI(klines, endIdx - 1, 14).doubleValue();
        boolean rsiBreakout = rsiYesterday < 45.0 && rsiToday >= 48.0;

        // 3. MACD 底背離檢測
        boolean macdDivergence = checkMacdBottomDivergence(klines, endIdx);

        // 動能要求：滿足 MACD 底背離，或者（KDJ 低位金叉 + RSI 低位突破）
        return macdDivergence || (kdjBottomCross && rsiBreakout);
    }

    /**
     * 檢測 MACD 底背離 (Simplified)
     */
    private boolean checkMacdBottomDivergence(List<TfKline> klines, int endIdx) {
        double[] macdToday = calculateMACD(klines, endIdx);
        double[] macdPrior = calculateMACD(klines, endIdx - 20);

        // 當前價格 <= 20 日前的價格（股價走低/新低）
        boolean priceLower = klines.get(endIdx).getClose().compareTo(klines.get(endIdx - 20).getClose()) <= 0;
        // 但當前 DIF > 20 日前的 DIF（指標抬升）
        boolean difHigher = macdToday[0] > macdPrior[0];

        return priceLower && difHigher;
    }

    // =========================================================================
    // 底層技術指標計算小工具
    // =========================================================================

    private BigDecimal calculateMA(List<TfKline> klines, int endIdx, int window) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = endIdx - window + 1; i <= endIdx; i++) {
            sum = sum.add(klines.get(i).getClose());
        }
        return sum.divide(BigDecimal.valueOf(window), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolMA(List<TfKline> klines, int endIdx, int window) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = endIdx - window + 1; i <= endIdx; i++) {
            sum = sum.add(klines.get(i).getVolume());
        }
        return sum.divide(BigDecimal.valueOf(window), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxHigh(List<TfKline> klines, int endIdx, int window) {
        BigDecimal maxHigh = klines.get(endIdx).getHigh();
        for (int i = endIdx - window + 1; i <= endIdx; i++) {
            if (klines.get(i).getHigh().compareTo(maxHigh) > 0) maxHigh = klines.get(i).getHigh();
        }
        return maxHigh;
    }

    private BigDecimal calculateMinLow(List<TfKline> klines, int endIdx, int window) {
        BigDecimal minLow = klines.get(endIdx).getLow();
        for (int i = endIdx - window + 1; i <= endIdx; i++) {
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
            ema12 = ema12 * 11 / 13 + close * 2 / 13;
            ema26 = ema26 * 25 / 27 + close * 2 / 27;
            double dif = ema12 - ema26;
            dea = dea * 8 / 10 + dif * 2 / 10;

            if (i == endIdx) {
                return new double[]{dif, dea, (dif - dea) * 2};
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

        for (int i = endIdx - period + 1; i <= endIdx; i++) {
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