package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.stock.strategy.interfaces.StockPickerStrategy;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 策略一
 * 強勢突破策略 StrongBreakoutStrategy
 * 適用大盤態勢：STRONG_UPTREND（主升浪）
 */
@Component
public class StrongBreakoutStrategy implements StockPickerStrategy {

    @Override
    public String getStrategyName() {
        return "主升浪-VCP/均線粘合/空中加油多維共振突破策略";
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
        // 第一分冊：核心選股形態（三者滿足其一）
        // ==========================================
        boolean matchVcp = checkVcpBreakout(klines, lastIdx);
        boolean matchMaCoil = checkMaCoilBreakout(klines, lastIdx);
        boolean matchAirRefuel = checkAirRefueling(klines, lastIdx);

        if (!matchVcp && !matchMaCoil && !matchAirRefuel) {
            return false;
        }

        // ==========================================
        // 第二分冊：技術指標信號交叉驗證（需滿足動能條件）
        // ==========================================
        // 1. MACD 零軸上方二次金叉 或 DIF/DEA 在零軸之上
        if (!checkMacdCondition(klines, lastIdx)) {
            return false;
        }

        // 2. KDJ 在 50 附近金叉，且 RSI 低於 80 (避開極端超買)
        if (!checkKdjRsiCondition(klines, lastIdx)) {
            return false;
        }

        // 3. OBV 量在價先（OBV 創近 20 日新高）
        if (!checkObvCondition(klines, lastIdx)) {
            return false;
        }

        // ==========================================
        // 第三分冊：籌碼面與盤口特徵（若有 Context 數據則強校驗）
        // ==========================================
        if (context != null) {
            // 籌碼獲利比例 >= 80%
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.80) {
                return false;
            }
            // 競價高開異動判定 (高開 1% - 3.5%)
            if (context.getAuctionCallChangeRate() != null) {
                double auctionChange = context.getAuctionCallChangeRate().doubleValue();
                if (auctionChange < 0.01 || auctionChange > 0.035) {
                    return false;
                }
            }
        }

        return true;
    }

    // =========================================================================
    // 形態判定邏輯
    // =========================================================================

    /**
     * 1.1 窄幅橫盤突破 (VCP)
     * - 前期1-3周 (10-20日) 振幅收窄 (< 8%)，成交量縮至地量
     * - 當日大陽線 (漲幅 >= 3%)，成交量 >= 前5日均量 2 倍，突破箱體上軌
     */
    private boolean checkVcpBreakout(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 漲幅 >= 3%
        BigDecimal changeRate = today.getClose().subtract(yesterday.getClose())
                .divide(yesterday.getClose(), 4, RoundingMode.HALF_UP);
        if (changeRate.doubleValue() < 0.03) return false;

        // 前 10 個交易日振幅收窄 < 10%
        BigDecimal maxHigh10 = calculateMaxHigh(klines, endIdx - 1, 10);
        BigDecimal minLow10 = calculateMinLow(klines, endIdx - 1, 10);
        BigDecimal amplitude = maxHigh10.subtract(minLow10).divide(minLow10, 4, RoundingMode.HALF_UP);
        if (amplitude.doubleValue() > 0.10) return false;

        // 當日放量 >= 2 * Vol_MA5
        BigDecimal volMa5 = calculateVolMA(klines, endIdx - 1, 5);
        if (today.getVolume().compareTo(volMa5.multiply(new BigDecimal("2.0"))) < 0) return false;

        // 突破近 15 日最高價
        BigDecimal high15 = calculateMaxHigh(klines, endIdx - 1, 15);
        return today.getClose().compareTo(high15) > 0;
    }

    /**
     * 1.2 均線多頭粘合與突破
     * - MA5, MA10, MA20, MA30 相互粘合 (極差 / MA20 < 2.5%)
     * - 一陽穿多線：當日陽線穿過 MA5, MA10, MA20, MA30，且 MA5 金叉 MA10/MA20
     */
    private boolean checkMaCoilBreakout(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        BigDecimal ma5 = calculateMA(klines, endIdx, 5);
        BigDecimal ma10 = calculateMA(klines, endIdx, 10);
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma30 = calculateMA(klines, endIdx, 30);

        // 昨日 5,10,20,30 均線高度粘合
        BigDecimal prevMa5 = calculateMA(klines, endIdx - 1, 5);
        BigDecimal prevMa10 = calculateMA(klines, endIdx - 1, 10);
        BigDecimal prevMa20 = calculateMA(klines, endIdx - 1, 20);
        BigDecimal prevMa30 = calculateMA(klines, endIdx - 1, 30);

        BigDecimal maxMa = prevMa5.max(prevMa10).max(prevMa20).max(prevMa30);
        BigDecimal minMa = prevMa5.min(prevMa10).min(prevMa20).min(prevMa30);
        BigDecimal diffRatio = maxMa.subtract(minMa).divide(prevMa20, 4, RoundingMode.HALF_UP);

        if (diffRatio.doubleValue() > 0.025) return false; // 粘合度不夠

        // 一陽穿多線：開盤價在多條均線下方或附近，收盤價站在所有均線上
        boolean isOneSunCross = today.getClose().compareTo(ma5) > 0
                && today.getClose().compareTo(ma10) > 0
                && today.getClose().compareTo(ma20) > 0
                && today.getClose().compareTo(ma30) > 0
                && today.getOpen().compareTo(maxMa) <= 0;

        // MA5 上穿 MA10/MA20 (金叉)
        boolean ma5Cross = prevMa5.compareTo(prevMa10) <= 0 && ma5.compareTo(ma10) > 0;

        return isOneSunCross && ma5Cross;
    }

    /**
     * 1.3 空中加油與主力洗盤（反包形態）
     * - 前一日縮量陰線（或回踩 MA10/MA20 不破）
     * - 當日大陽線實體完全包覆前一日陰線實體（陽包陰）
     */
    private boolean checkAirRefueling(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 昨日為陰線 (Close < Open)
        boolean yesterdayIsYin = yesterday.getClose().compareTo(yesterday.getOpen()) < 0;
        if (!yesterdayIsYin) return false;

        // 昨日回踩 MA10 或 MA20 不破 (Low >= MA20 * 0.99)
        BigDecimal ma20 = calculateMA(klines, endIdx - 1, 20);
        if (yesterday.getLow().compareTo(ma20.multiply(new BigDecimal("0.99"))) < 0) return false;

        // 當日強勢反包：今日收盤 > 昨日開盤 且 今日開盤 <= 昨日收盤
        boolean isPackage = today.getClose().compareTo(yesterday.getOpen()) > 0
                && today.getOpen().compareTo(yesterday.getClose()) <= 0;

        // 當日成交量放大
        boolean isVolExpand = today.getVolume().compareTo(yesterday.getVolume()) > 0;

        return isPackage && isVolExpand;
    }

    // =========================================================================
    // 技術指標校驗邏輯
    // =========================================================================

    /**
     * 2.1 MACD 零軸上方金叉 或 多頭伸長
     */
    private boolean checkMacdCondition(List<TfKline> klines, int endIdx) {
        double[] macdToday = calculateMACD(klines, endIdx);
        double[] macdYesterday = calculateMACD(klines, endIdx - 1);

        double dif = macdToday[0];
        double dea = macdToday[1];
        double hist = macdToday[2];

        double prevDif = macdYesterday[0];
        double prevDea = macdYesterday[1];

        // DIF 與 DEA 位於零軸上方（或貼近零軸 >= -0.05）
        boolean nearZeroOrAbove = dif >= -0.05 && dea >= -0.05;

        // 當日金叉（DIF 上穿 DEA）或 紅柱伸長 (Hist > 0 且 Hist > PrevHist)
        boolean isGoldenCross = prevDif <= prevDea && dif > dea;
        boolean isRedHistExpanding = hist > 0 && hist > macdYesterday[2];

        return nearZeroOrAbove && (isGoldenCross || isRedHistExpanding);
    }

    /**
     * 2.2 KDJ 在 50 附近金叉，且 RSI < 80 (避開極端超買)
     */
    private boolean checkKdjRsiCondition(List<TfKline> klines, int endIdx) {
        double[] kdjToday = calculateKDJ(klines, endIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, endIdx - 1, 9, 3, 3);

        double k = kdjToday[0];
        double d = kdjToday[1];
        double prevK = kdjYesterday[0];
        double prevD = kdjYesterday[1];

        // KDJ 金叉，且 K 值在 40-70 的強勢區間（非低位鈍化，也非極端高位）
        boolean kdjCross = prevK <= prevD && k > d;
        boolean inStrongZone = k >= 40.0 && k <= 75.0;

        // RSI(14) < 80 避開動能衰竭
        double rsi14 = calculateRSI(klines, endIdx, 14).doubleValue();
        boolean rsiSafe = rsi14 < 80.0 && rsi14 > 45.0;

        return kdjCross && inStrongZone && rsiSafe;
    }

    /**
     * 2.3 OBV 能量潮創 20 日新高 (量在價先)
     */
    private boolean checkObvCondition(List<TfKline> klines, int endIdx) {
        BigDecimal todayObv = calculateOBV(klines, endIdx);
        BigDecimal maxObv20 = todayObv;

        for (int i = endIdx - 19; i < endIdx; i++) {
            BigDecimal obv = calculateOBV(klines, i);
            if (obv.compareTo(maxObv20) > 0) {
                maxObv20 = obv;
            }
        }
        // 今日 OBV 創出近 20 日新高
        return todayObv.compareTo(maxObv20) >= 0;
    }

    // =========================================================================
    // 底層技術指標計算小工具 (包含 MACD, KDJ, OBV, RSI, MA)
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

    /**
     * 計算 MACD (12, 26, 9) -> [DIF, DEA, HIST]
     */
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
                double hist = (dif - dea) * 2;
                return new double[]{dif, dea, hist};
            }
        }
        return new double[]{0, 0, 0};
    }

    /**
     * 計算 KDJ (9, 3, 3) -> [K, D, J]
     */
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
        double j = 3.0 * k - 2.0 * d;
        return new double[]{k, d, j};
    }

    /**
     * 計算 OBV 能量潮
     */
    private BigDecimal calculateOBV(List<TfKline> klines, int endIdx) {
        BigDecimal obv = BigDecimal.ZERO;
        for (int i = 1; i <= endIdx; i++) {
            BigDecimal closeToday = klines.get(i).getClose();
            BigDecimal closeYesterday = klines.get(i - 1).getClose();
            BigDecimal vol = klines.get(i).getVolume();

            if (closeToday.compareTo(closeYesterday) > 0) {
                obv = obv.add(vol);
            } else if (closeToday.compareTo(closeYesterday) < 0) {
                obv = obv.subtract(vol);
            }
        }
        return obv;
    }

    /**
     * 計算 RSI(N)
     */
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