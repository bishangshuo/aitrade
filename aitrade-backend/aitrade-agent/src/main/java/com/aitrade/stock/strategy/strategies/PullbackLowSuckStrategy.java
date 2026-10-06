package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 弱上升/震盪攀升（WEAK_UPTREND）環境下的「趨勢-動能-量價」共振低吸策略
 */
@Component
public class PullbackLowSuckStrategy implements StockPickerStrategy {

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
        if (klines == null || klines.size() < 120) return false;

        int lastIdx = klines.size() - 1;

        // ==========================================
        // 1. 趨勢條件（Trend）：確認處於 WEAK_UPTREND 震盪攀升通道
        // ==========================================
        if (!checkWeakUptrendChannel(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 2. 量價條件（Volume-Price）：縮量回踩支撐位 + 止跌形態
        // ==========================================
        if (!checkVolumePricePullback(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 3. 動能條件（Momentum）：超賣修復 / 低位金叉
        // ==========================================
        if (!checkMomentumOversold(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 4. 籌碼與盤口輔助（可選 Context 數據）
        // ==========================================
        if (context != null) {
            // 震盪低吸不宜高開過高，優先選擇平開或微幅低開/高開（-1.5% ~ +1.0%）
            if (context.getAuctionCallChangeRate() != null) {
                double auctionChange = context.getAuctionCallChangeRate().doubleValue();
                if (auctionChange > 0.015 || auctionChange < -0.02) {
                    return false; // 避開大幅開高（容易補跌）或大幅低開（趨勢破位風險）
                }
            }
        }

        return true;
    }

    // =========================================================================
    // 1. 趨勢驗證邏輯
    // =========================================================================

    /**
     * 驗證 WEAK_UPTREND 震盪攀升通道：
     * - 中長期均線（MA20, MA60）呈溫和多頭排列且角度向上。
     * - 近 60 個交易日高點與低點逐步墊高（Higher Highs & Higher Lows）。
     * - 股價振幅適中，未出現暴漲暴跌（斜率平緩）。
     */
    private boolean checkWeakUptrendChannel(List<TfKline> klines, int endIdx) {
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

        return minLowRecent.compareTo(minLowPrior) > 0;
    }

    // =========================================================================
    // 2. 量價回踩與止跌邏輯
    // =========================================================================

    /**
     * 驗證縮量回踩與支撐：
     * - 股價回踩至重要支撐位附近（MA20 或 MA30 附近，容許 ±1.5% 誤差）。
     * - 回踩過程成交量明顯萎縮（當日或前一日成交量 < 近 10 日平均成交量的 70%）。
     * - 當日出現止跌信號（陽線收盤，或下影線長度 > 實體長度的 1.5 倍）。
     */
    private boolean checkVolumePricePullback(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma30 = calculateMA(klines, endIdx, 30);

        // 1. 回踩支撐位：最低價或收盤價接近 MA20/MA30
        BigDecimal distToMa20 = today.getLow().subtract(ma20).abs().divide(ma20, 4, RoundingMode.HALF_UP);
        BigDecimal distToMa30 = today.getLow().subtract(ma30).abs().divide(ma30, 4, RoundingMode.HALF_UP);
        boolean touchSupport = distToMa20.doubleValue() <= 0.015 || distToMa30.doubleValue() <= 0.015;

        if (!touchSupport) return false;

        // 2. 縮量特徵：當日成交量 < 近 10 日均量的 75%
        BigDecimal volMa10 = calculateVolMA(klines, endIdx - 1, 10);
        boolean isShrinkVolume = today.getVolume().compareTo(volMa10.multiply(new BigDecimal("0.75"))) < 0;

        if (!isShrinkVolume) return false;

        // 3. 止跌信號：當日收陽線 (Close >= Open) 或帶有明顯下影線
        boolean isRedCandle = today.getClose().compareTo(today.getOpen()) >= 0;

        BigDecimal body = today.getClose().subtract(today.getOpen()).abs();
        BigDecimal lowerShadow = today.getOpen().min(today.getClose()).subtract(today.getLow());
        boolean hasLongLowerShadow = lowerShadow.compareTo(body.multiply(new BigDecimal("1.5"))) > 0;

        return isRedCandle || hasLongLowerShadow;
    }

    // =========================================================================
    // 3. 動能指標低位/超賣邏輯
    // =========================================================================

    /**
     * 驗證動能指標超賣與修復：
     * - KDJ：J 線/K 線進入低位區域（K < 35 或 KDJ 在 30 附近金叉/勾頭向上）。
     * - RSI：RSI(14) 回踩至 35 - 50 的多頭超卖/合理低吸區（未跌破 30 弱勢區）。
     * - MACD：DIF 在 0 軸上方回踩 DEA 不破（水上加油/回踩 0 軸止跌）。
     */
    private boolean checkMomentumOversold(List<TfKline> klines, int endIdx) {
        // 1. KDJ 計算
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);

        double k = kdjToday[0];
        double d = kdjToday[1];
        double prevK = kdjYesterday[0];
        double prevD = kdjYesterday[1];

        // 低位金叉 或 K線低位勾頭向上
        boolean kdjCrossOrUp = (prevK <= prevD && k > d) || (k < 40 && k > prevK);

        // 2. RSI 計算 (RSI 14 在 35 ~ 52 之間)
        double rsi14 = calculateRSI(klines, endIdx, 14).doubleValue();
        boolean rsiOversoldZone = rsi14 >= 35.0 && rsi14 <= 52.0;

        // 3. MACD 計算 (DIF/DEA 保持在 0 軸上方，或貼近 0 軸)
        double[] macdToday = calculateMACD(klines, endIdx);
        double dif = macdToday[0];
        double dea = macdToday[1];
        boolean macdZeroSupport = dif >= -0.02 && dea >= -0.02;

        return kdjCrossOrUp && rsiOversoldZone && macdZeroSupport;
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