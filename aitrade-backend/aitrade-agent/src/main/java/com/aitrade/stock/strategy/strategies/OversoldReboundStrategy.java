package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
/**
 * 跌勢反彈（REBOUND_DOWNTREND）環境下的「趨勢-動能-量價」共振超跌反彈策略
 */
@Component
public class OversoldReboundStrategy implements StockPickerStrategy {

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
        if (klines == null || klines.size() < 120) return false;

        int lastIdx = klines.size() - 1;

        // ==========================================
        // 1. 趨勢條件（Trend）：空头趨勢下的短期負乖離過大（超跌偏離）
        // ==========================================
        if (!checkDowntrendOversoldBias(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 2. 動能條件（Momentum）：RSI/KDJ 極端超賣 + MACD 綠柱縮短
        // ==========================================
        if (!checkOversoldMomentum(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 3. 量價條件（Volume-Price）：急跌後的止跌反轉 K 線 + 資金流入放量
        // ==========================================
        if (!checkVolumePriceReversal(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 4. 籌碼面與盤口輔助（可選 Context）
        // ==========================================
        if (context != null) {
            // 超跌反彈通常需要開盤有恐慌盤殺出後拉升，或者平開/微幅高開搶籌
            if (context.getAuctionCallChangeRate() != null) {
                double auctionChange = context.getAuctionCallChangeRate().doubleValue();
                // 避開跌停開盤（破位風險）或大幅高開 > 3%（反彈空間被大幅壓縮）
                if (auctionChange < -0.05 || auctionChange > 0.03) {
                    return false;
                }
            }
        }

        return true;
    }

    // =========================================================================
    // 1. 趨勢與乖離率（Bias）驗證邏輯
    // =========================================================================

    /**
     * 驗證跌勢超跌偏離度：
     * - MA20, MA60 呈空頭排列（MA20 < MA60）。
     * - 近 5-10 個交易日經歷快速下跌。
     * - 20 日乖離率 (BIAS20) < -8%（即股價低於 MA20 超過 8% 以上），存在強烈的均線回吸拉力。
     */
    private boolean checkDowntrendOversoldBias(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);

        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma60 = calculateMA(klines, endIdx, 60);

        // 中短期均線呈空頭架構
        if (ma20.compareTo(ma60) >= 0) {
            return false;
        }

        // BIAS20 = (Close - MA20) / MA20
        BigDecimal bias20 = today.getClose().subtract(ma20).divide(ma20, 4, RoundingMode.HALF_UP);

        // 負乖離率需低於 -8%（例如 -0.08）
        return bias20.doubleValue() <= -0.08;
    }

    // =========================================================================
    // 2. 動能極端超賣驗證邏輯
    // =========================================================================

    /**
     * 驗證動能指標超賣與衰竭修復：
     * - RSI(6) < 25 或 RSI(14) < 32 (進入極端超賣區，空頭動能衰竭)。
     * - KDJ 的 K 值 < 20，且當日 K 線在低位開始勾頭向上或形成金叉。
     * - MACD 綠柱（HIST）較前一日縮短（代表殺跌動能減弱）。
     */
    private boolean checkOversoldMomentum(List<TfKline> klines, int endIdx) {
        // 1. RSI 6 和 14 計算
        double rsi6 = calculateRSI(klines, endIdx, 6).doubleValue();
        double rsi14 = calculateRSI(klines, endIdx, 14).doubleValue();
        boolean isRsiOversold = rsi6 < 28.0 || rsi14 < 32.0;

        if (!isRsiOversold) return false;

        // 2. KDJ 計算 (K 值在低位 25 以下，且 K > PrevK 勾頭向上)
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);

        boolean isKdjLowHook = kdjYesterday[0] < 25.0 && kdjToday[0] > kdjYesterday[0];

        // 3. MACD 綠柱縮短 (Hist < 0 且 Hist > PrevHist)
        double[] macdToday = calculateMACD(klines, endIdx);
        double[] macdYesterday = calculateMACD(klines, endIdx - 1);

        boolean isMacdGreenShortening = macdToday[2] < 0 && macdToday[2] > macdYesterday[2];

        return isKdjLowHook && isMacdGreenShortening;
    }

    // =========================================================================
    // 3. 量價止跌與抄底反轉邏輯
    // =========================================================================

    /**
     * 驗證量價止跌反轉形態：
     * - 形態 A: 長下影線（錘頭線/金針探底，下影線長度 >= 實體 2 倍）。
     * - 形態 B: 當日大陽線反包前一日陰線（陽包陰/曙光初現），漲幅 >= 2.5%。
     * - 成交量要求：止跌當日成交量放量（>= 近 5 日平均成交量的 1.3 倍，代表有抄底資金接盤）。
     */
    private boolean checkVolumePriceReversal(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 1. 放量驗證：抄底資金介入，成交量 >= 1.3 * Vol_MA5
        BigDecimal volMa5 = calculateVolMA(klines, endIdx - 1, 5);
        if (today.getVolume().compareTo(volMa5.multiply(new BigDecimal("1.3"))) < 0) {
            return false;
        }

        // 2. K 線形態判定：
        // 形態 A：錘頭線 / 長下影線
        BigDecimal body = today.getClose().subtract(today.getOpen()).abs();
        BigDecimal lowerShadow = today.getOpen().min(today.getClose()).subtract(today.getLow());
        boolean isHammerLine = lowerShadow.compareTo(body.multiply(new BigDecimal("2.0"))) > 0;

        // 形態 B：陽包陰（今日陽線，收盤價超過昨日陰線實體一半以上）
        boolean yesterdayIsYin = yesterday.getClose().compareTo(yesterday.getOpen()) < 0;
        BigDecimal yesterdayMidPoint = yesterday.getOpen().add(yesterday.getClose()).divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
        boolean isSunWrapYin = today.getClose().compareTo(today.getOpen()) > 0
                && yesterdayIsYin
                && today.getClose().compareTo(yesterdayMidPoint) > 0;

        // 漲幅 >= 2.0%
        BigDecimal changeRate = today.getClose().subtract(yesterday.getClose())
                .divide(yesterday.getClose(), 4, RoundingMode.HALF_UP);
        boolean strongBounce = changeRate.doubleValue() >= 0.02;

        return (isHammerLine || isSunWrapYin) && strongBounce;
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