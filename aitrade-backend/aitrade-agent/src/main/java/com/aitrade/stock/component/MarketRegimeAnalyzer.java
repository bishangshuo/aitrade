package com.aitrade.stock.component;

import com.aitrade.stock.domain.MarketRegimeResult;
import com.aitrade.stock.enums.MarketRegimeEnum;
import com.aitrade.tickflow.domain.TfKline;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class MarketRegimeAnalyzer {

    public MarketRegimeResult analyze(List<TfKline> klineList) {
        if (klineList == null || klineList.size() < 120) {
            throw new IllegalArgumentException("分析數據不足，至少需要 120 根日 K 線數據");
        }

        int lastIdx = klineList.size() - 1;
        BigDecimal cPrice = klineList.get(lastIdx).getClose();

        // 1. 計算各周期均線
        BigDecimal ma5 = calculateMA(klineList, lastIdx, 5);
        BigDecimal ma20 = calculateMA(klineList, lastIdx, 20);
        BigDecimal ma60 = calculateMA(klineList, lastIdx, 60);

        // 5日前的 MA（用於計算斜率）
        BigDecimal ma20Prev5 = calculateMA(klineList, lastIdx - 5, 20);
        BigDecimal ma60Prev5 = calculateMA(klineList, lastIdx - 5, 60);

        // 2. 計算關鍵指標
        // 斜率 (%)
        BigDecimal ma20Slope = ma20.subtract(ma20Prev5).divide(ma20Prev5, 6, RoundingMode.HALF_UP);
        BigDecimal ma60Slope = ma60.subtract(ma60Prev5).divide(ma60Prev5, 6, RoundingMode.HALF_UP);

        // BIAS20 乖離率 (%) = (Close - MA20) / MA20
        BigDecimal bias20 = cPrice.subtract(ma20).divide(ma20, 6, RoundingMode.HALF_UP);

        // 波動率與通道
        BigDecimal range20 = calculateRange20(klineList, lastIdx, ma20);
        BigDecimal bollBandwidth = calculateBollBandwidth20(klineList, lastIdx, ma20);

        // 250日位置 & RSI(14)
        BigDecimal pos250 = calculate250DayPosition(klineList, lastIdx, cPrice);
        BigDecimal rsi14 = calculateRSI(klineList, lastIdx, 14);

        // 3. 多維度態勢分類邏輯 (樹狀過濾)
        MarketRegimeEnum regime;

        double slope20Val = ma20Slope.doubleValue();
        double slope60Val = ma60Slope.doubleValue();
        double bias20Val = bias20.doubleValue();
        double range20Val = range20.doubleValue();
        double bandwidthVal = bollBandwidth.doubleValue();
        double pos250Val = pos250.doubleValue();
        double rsi14Val = rsi14.doubleValue();

        // --- A. 上升體系 ---
        if (cPrice.compareTo(ma20) > 0 && ma20.compareTo(ma60) > 0) {
            // A1. 加速衝頂/情緒高潮：偏離 MA20 過遠 或 RSI 進入極端超買區
            if (bias20Val > 0.08 || rsi14Val > 75.0) {
                regime = MarketRegimeEnum.CLIMAX_UPTREND;
            }
            // A2. 主升浪（強上升）：均線順序多頭且斜率陡峭
            else if (cPrice.compareTo(ma5) >= 0 && ma5.compareTo(ma20) > 0 && slope20Val > 0.004) {
                regime = MarketRegimeEnum.STRONG_UPTREND;
            }
            // A3. 震盪攀升（弱上升）：上行斜率較緩
            else {
                regime = MarketRegimeEnum.WEAK_UPTREND;
            }
        }
        // --- B. 下跌體系 ---
        else if (cPrice.compareTo(ma20) < 0 && ma20.compareTo(ma60) < 0) {
            // B1. 超跌反彈（超賣/嚴重背離）：短期跌幅過深，存在修復需求
            if (bias20Val < -0.08 || rsi14Val < 25.0) {
                regime = MarketRegimeEnum.REBOUND_DOWNTREND;
            }
            // B2. 主跌浪（強下跌）：短期均線向下發散且斜率陡峭
            else if (cPrice.compareTo(ma5) <= 0 && ma5.compareTo(ma20) < 0 && slope20Val < -0.004) {
                regime = MarketRegimeEnum.STRONG_DOWNTREND;
            }
            // B3. 陰跌修復（弱下跌）：緩慢下滑
            else {
                regime = MarketRegimeEnum.WEAK_DOWNTREND;
            }
        }
        // --- C. 橫盤與盤整體系 ---
        else if (range20Val < 0.06 || bandwidthVal < 0.05) {
            if (pos250Val < 0.30) {
                regime = MarketRegimeEnum.LOW_CONSOLIDATION;
            } else if (pos250Val > 0.70) {
                regime = MarketRegimeEnum.HIGH_CONSOLIDATION;
            } else {
                regime = MarketRegimeEnum.RANGE_BOUND;
            }
        }
        // --- D. 其餘情況歸為箱體震盪 ---
        else {
            regime = MarketRegimeEnum.RANGE_BOUND;
        }

        return MarketRegimeResult.builder()
                .regime(regime)
                .regimeName(regime.getName())
                .description(regime.getDescription())
                .strategy(regime.getStrategy())
                .latestClose(cPrice)
                .bias20Percent(bias20.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .ma20SlopePercent(ma20Slope.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .ma60SlopePercent(ma60Slope.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .position250dPercent(pos250.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .bollBandwidthPercent(bollBandwidth.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .range20dPercent(range20.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .rsi14(rsi14.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    // ==================== 輔助計算公式 ====================

    private BigDecimal calculateMA(List<TfKline> klines, int endIdx, int window) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = endIdx - window + 1; i <= endIdx; i++) {
            sum = sum.add(klines.get(i).getClose());
        }
        return sum.divide(BigDecimal.valueOf(window), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRange20(List<TfKline> klines, int endIdx, BigDecimal ma20) {
        BigDecimal maxHigh = klines.get(endIdx).getHigh();
        BigDecimal minLow = klines.get(endIdx).getLow();

        for (int i = endIdx - 19; i <= endIdx; i++) {
            TfKline kline = klines.get(i);
            if (kline.getHigh().compareTo(maxHigh) > 0) maxHigh = kline.getHigh();
            if (kline.getLow().compareTo(minLow) < 0) minLow = kline.getLow();
        }
        return maxHigh.subtract(minLow).divide(ma20, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBollBandwidth20(List<TfKline> klines, int endIdx, BigDecimal ma20) {
        BigDecimal varianceSum = BigDecimal.ZERO;
        for (int i = endIdx - 19; i <= endIdx; i++) {
            BigDecimal diff = klines.get(i).getClose().subtract(ma20);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        double variance = varianceSum.divide(BigDecimal.valueOf(20), 8, RoundingMode.HALF_UP).doubleValue();
        return BigDecimal.valueOf(4 * Math.sqrt(variance)).divide(ma20, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculate250DayPosition(List<TfKline> klines, int endIdx, BigDecimal currentClose) {
        int startIdx = Math.max(0, endIdx - 249);
        BigDecimal maxHigh = klines.get(endIdx).getHigh();
        BigDecimal minLow = klines.get(endIdx).getLow();

        for (int i = startIdx; i <= endIdx; i++) {
            TfKline kline = klines.get(i);
            if (kline.getHigh().compareTo(maxHigh) > 0) maxHigh = kline.getHigh();
            if (kline.getLow().compareTo(minLow) < 0) minLow = kline.getLow();
        }

        BigDecimal diff = maxHigh.subtract(minLow);
        if (diff.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("0.5");
        return currentClose.subtract(minLow).divide(diff, 6, RoundingMode.HALF_UP);
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
        // RSI = 100 - (100 / (1 + RS))
        BigDecimal hundred = new BigDecimal("100");
        BigDecimal onePlusRs = BigDecimal.ONE.add(rs);
        return hundred.subtract(hundred.divide(onePlusRs, 6, RoundingMode.HALF_UP));
    }
}