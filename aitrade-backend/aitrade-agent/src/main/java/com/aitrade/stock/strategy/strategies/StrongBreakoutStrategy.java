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
 * 主升浪-VCP/均線粘合/空中加油多維共振突破策略
 *
 * 核心設計理念：
 * 1. 形態驅動：尋找主力建倉完成、籌碼高度集中後的突破臨界點（VCP、均線粘合、空中加油三選一）。
 * 2. 動能共振：通過 MACD、KDJ、RSI 與 OBV 能量潮確認中短線多頭動能與資金真實流入。
 * 3. 籌碼過濾：結合獲利盤比例與單峰密集特徵，排除上方套牢盤過重的標的。
 * 4. 診斷反饋：將所有過濾過程與中間指標完整記錄，支援回測分析與參數優化。
 */
@Component
public class StrongBreakoutStrategy implements StockPickerStrategy {

    @Override
    public Integer getStrategyCode() {
        return StrategyType.STRONG_BREAKOUT.getCode();
    }

    @Override
    public String getStrategyName() {
        return "主升浪-VCP/均線粘合/空中加油多維共振突破策略";
    }

    /**
     * 相容原有策略接口：單純返回是否通過篩選
     */
    @Override
    public boolean filter(List<TfKline> klines) {
        return filter(klines, null);
    }

    /**
     * 相容原有策略接口：帶上下文的布林過濾
     */
    @Override
    public boolean filter(List<TfKline> klines, StockAnalysisContext context) {
        // 內部直接調用完整分析方法，並僅返回 passed 狀態
        StockPickDiagnostic diagnostic = analyze("UNKNOWN", "未知", LocalDate.now(), klines, context);
        return diagnostic.getPassed().equals(1);
    }

    /**
     * 核心增強分析方法：執行形態篩選、技術指標校驗、多維度評分，並輸出完整的診斷明細
     *
     * @param symbol      股票代碼
     * @param stockName   股票名稱
     * @param tradeDate   交易日期
     * @param klines      K線歷史數據列表（要求至少包含最近 120 個交易日）
     * @param context     盤口與籌碼分析上下文數據
     * @return            包含所有指標與得分的診斷記錄實體
     */
    @Override
    public StockPickDiagnostic analyze(
            String symbol,
            String stockName,
            LocalDate tradeDate,
            List<TfKline> klines, StockAnalysisContext context
    ) {
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

        // 計算 5 日均量，作為放量判定基準
        BigDecimal volMa5 = calculateVolMA(klines, lastIdx - 1, 5);
        BigDecimal volRatio = volMa5.compareTo(BigDecimal.ZERO) > 0 ?
                today.getVolume().divide(volMa5, 2, RoundingMode.HALF_UP) : BigDecimal.ONE;
        builder.volRatio(volRatio);

        // ==========================================
        // 第一分冊：核心選股形態檢查（三者滿足其一即可）
        // ==========================================
        boolean matchVcp = checkVcpBreakout(klines, lastIdx, changeRate, volMa5, builder);
        boolean matchMaCoil = checkMaCoilBreakout(klines, lastIdx);
        boolean matchAirRefuel = checkAirRefueling(klines, lastIdx);

        // 將形態匹配標記寫入構建器
        builder.matchVcp(matchVcp)
                .matchMaCoil(matchMaCoil)
                .matchAirRefuel(matchAirRefuel);

        // 若三種核心形態全不滿足，直接攔截退出（Fail-Fast）
        if (!matchVcp && !matchMaCoil && !matchAirRefuel) {
            return builder.failReason("PATTERN_NO_MATCH").build();
        }

        // ==========================================
        // 第二分冊：技術指標信號交叉驗證
        // ==========================================

        // 1. MACD 指標校驗
        double[] macdToday = calculateMACD(klines, lastIdx);
        double[] macdYesterday = calculateMACD(klines, lastIdx - 1);
        builder.macdDif(BigDecimal.valueOf(macdToday[0]))
                .macdDea(BigDecimal.valueOf(macdToday[1]))
                .macdHist(BigDecimal.valueOf(macdToday[2]));

        if (!checkMacdCondition(macdToday, macdYesterday)) {
            return builder.failReason("MACD_CONDITION_FAIL").build();
        }

        // 2. KDJ 與 RSI 指標校驗
        double[] kdjToday = calculateKDJ(klines, lastIdx, 9, 3, 3);
        double[] kdjYesterday = calculateKDJ(klines, lastIdx - 1, 9, 3, 3);
        BigDecimal rsi14 = calculateRSI(klines, lastIdx, 14);
        builder.kdjK(BigDecimal.valueOf(kdjToday[0]))
                .kdjD(BigDecimal.valueOf(kdjToday[1]))
                .rsi14(rsi14);

        if (!checkKdjRsiCondition(kdjToday, kdjYesterday, rsi14)) {
            return builder.failReason("KDJ_RSI_CONDITION_FAIL").build();
        }

        // 3. OBV 能量潮校驗
        BigDecimal todayObv = calculateOBV(klines, lastIdx);
        builder.obv(todayObv);
        if (!checkObvCondition(klines, lastIdx, todayObv)) {
            return builder.failReason("OBV_NOT_NEW_HIGH").build();
        }

        // ==========================================
        // 第三分冊：籌碼面與盤口特徵校驗（若有 Context 數據）
        // ==========================================
        if (context != null) {
            builder.profitRatio(context.getProfitRatio())
                    .isChipSinglePeak(context.getIsChipSinglePeak());

            // 獲利盤比例低於 70% 排除，避免上方套牢盤沉重導致上攻乏力
            if (context.getProfitRatio() != null && context.getProfitRatio().doubleValue() < 0.70) {
                return builder.failReason("PROFIT_RATIO_LOW").build();
            }
        }

        // ==========================================
        // 第四分冊：完全通過過濾，執行多維度綜合評分
        // ==========================================
        calculateAndSetScores(builder, changeRate, volRatio, context, macdToday);

        // 最終標記為通過篩選
        return builder.passed(1)
                .failReason(null)
                .build();
    }

    // =========================================================================
    // 形態判定邏輯模組
    // =========================================================================

    /**
     * 1.1 窄幅橫盤突破形態 (VCP - Volatility Contraction Pattern)
     * 條件：前期 10 日振幅收窄 < 10%，當日漲幅 >= 3%，放量突破 2 倍 5 日均量，且突破前高。
     */
    private boolean checkVcpBreakout(List<TfKline> klines, int endIdx, BigDecimal changeRate,
                                     BigDecimal volMa5, StockPickDiagnostic.StockPickDiagnosticBuilder builder) {
        if (changeRate.doubleValue() < 0.03) return false;

        // 計算前 10 個交易日的最高價與最低價振幅
        BigDecimal maxHigh10 = calculateMaxHigh(klines, endIdx - 1, 10);
        BigDecimal minLow10 = calculateMinLow(klines, endIdx - 1, 10);
        BigDecimal amplitude = maxHigh10.subtract(minLow10).divide(minLow10, 4, RoundingMode.HALF_UP);

        // 記錄 VCP 振幅至診斷實體
        builder.vcpAmplitude(amplitude);

        // 振幅大於 10% 說明收窄不夠徹底
        if (amplitude.doubleValue() > 0.10) return false;

        // 當日成交量需大於 2 倍 5 日均量
        TfKline today = klines.get(endIdx);
        if (today.getVolume().compareTo(volMa5.multiply(new BigDecimal("2.0"))) < 0) return false;

        // 當日收盤價需突破近 15 日最高價
        BigDecimal high15 = calculateMaxHigh(klines, endIdx - 1, 15);
        return today.getClose().compareTo(high15) > 0;
    }

    /**
     * 1.2 均線多頭粘合與突破形態
     * 條件：MA5, MA10, MA20, MA30 昨日高度粘合（極差小於 2.5%），當日一陽穿多線且 MA5 金叉 MA10。
     */
    private boolean checkMaCoilBreakout(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        BigDecimal ma5 = calculateMA(klines, endIdx, 5);
        BigDecimal ma10 = calculateMA(klines, endIdx, 10);
        BigDecimal ma20 = calculateMA(klines, endIdx, 20);
        BigDecimal ma30 = calculateMA(klines, endIdx, 30);

        BigDecimal prevMa5 = calculateMA(klines, endIdx - 1, 5);
        BigDecimal prevMa10 = calculateMA(klines, endIdx - 1, 10);
        BigDecimal prevMa20 = calculateMA(klines, endIdx - 1, 20);
        BigDecimal prevMa30 = calculateMA(klines, endIdx - 1, 30);

        // 計算昨日均線粘合度
        BigDecimal maxMa = prevMa5.max(prevMa10).max(prevMa20).max(prevMa30);
        BigDecimal minMa = prevMa5.min(prevMa10).min(prevMa20).min(prevMa30);
        BigDecimal diffRatio = maxMa.subtract(minMa).divide(prevMa20, 4, RoundingMode.HALF_UP);

        if (diffRatio.doubleValue() > 0.025) return false;

        // 一陽穿多線：收盤站上所有均線，開盤在均線附近或下方
        boolean isOneSunCross = today.getClose().compareTo(ma5) > 0
                && today.getClose().compareTo(ma10) > 0
                && today.getClose().compareTo(ma20) > 0
                && today.getClose().compareTo(ma30) > 0
                && today.getOpen().compareTo(maxMa) <= 0;

        // MA5 上穿 MA10 形成金叉
        boolean ma5Cross = prevMa5.compareTo(prevMa10) <= 0 && ma5.compareTo(ma10) > 0;

        return isOneSunCross && ma5Cross;
    }

    /**
     * 1.3 空中加油與主力洗盤反包形態
     * 條件：前一日縮量陰線且回踩 MA20 不破，當日大陽線實體完全包覆前一日陰線（陽包陰）且成交量放大。
     */
    private boolean checkAirRefueling(List<TfKline> klines, int endIdx) {
        TfKline today = klines.get(endIdx);
        TfKline yesterday = klines.get(endIdx - 1);

        // 昨日必須為陰線
        boolean yesterdayIsYin = yesterday.getClose().compareTo(yesterday.getOpen()) < 0;
        if (!yesterdayIsYin) return false;

        // 昨日回踩 MA20 不破（低點不低於 MA20 的 99%）
        BigDecimal ma20 = calculateMA(klines, endIdx - 1, 20);
        if (yesterday.getLow().compareTo(ma20.multiply(new BigDecimal("0.99"))) < 0) return false;

        // 當日強勢反包：收盤價高於昨日開盤，開盤價不低於昨日收盤
        boolean isPackage = today.getClose().compareTo(yesterday.getOpen()) > 0
                && today.getOpen().compareTo(yesterday.getClose()) <= 0;

        boolean isVolExpand = today.getVolume().compareTo(yesterday.getVolume()) > 0;

        return isPackage && isVolExpand;
    }

    // =========================================================================
    // 技術指標校驗邏輯模組
    // =========================================================================

    /**
     * 2.1 MACD 零軸上方金叉或紅柱伸長校驗
     */
    private boolean checkMacdCondition(double[] macdToday, double[] macdYesterday) {
        double dif = macdToday[0];
        double dea = macdToday[1];
        double hist = macdToday[2];

        // DIF 與 DEA 位於零軸上方或貼近零軸（>= -0.05）
        boolean nearZeroOrAbove = dif >= -0.05 && dea >= -0.05;
        // 當日金叉或紅柱繼續擴大
        boolean isGoldenCross = macdYesterday[0] <= macdYesterday[1] && dif > dea;
        boolean isRedHistExpanding = hist > 0 && hist > macdYesterday[2];

        return nearZeroOrAbove && (isGoldenCross || isRedHistExpanding);
    }

    /**
     * 2.2 KDJ 與 RSI 強勢區間校驗
     */
    private boolean checkKdjRsiCondition(double[] kdjToday, double[] kdjYesterday, BigDecimal rsi14) {
        double k = kdjToday[0];
        double d = kdjToday[1];

        // KDJ 金叉且 K 值處於 40-75 的強勢區間
        boolean kdjCross = kdjYesterday[0] <= kdjYesterday[1] && k > d;
        boolean inStrongZone = k >= 40.0 && k <= 75.0;
        // RSI(14) 保持在 45-80 之間，避免極端超買或動能衰竭
        boolean rsiSafe = rsi14.doubleValue() < 80.0 && rsi14.doubleValue() > 45.0;

        return kdjCross && inStrongZone && rsiSafe;
    }

    /**
     * 2.3 OBV 能量潮創 20 日新高校驗（量在價先）
     */
    private boolean checkObvCondition(List<TfKline> klines, int endIdx, BigDecimal todayObv) {
        BigDecimal maxObv20 = todayObv;
        for (int i = endIdx - 19; i < endIdx; i++) {
            BigDecimal obv = calculateOBV(klines, i);
            if (obv.compareTo(maxObv20) > 0) {
                maxObv20 = obv;
            }
        }
        return todayObv.compareTo(maxObv20) >= 0;
    }

    // =========================================================================
    // 多維度綜合評分計算模組
    // =========================================================================

    /**
     * 計算量價爆發力、籌碼面、技術動能與形態品質的加權得分
     */
    private void calculateAndSetScores(StockPickDiagnostic.StockPickDiagnosticBuilder builder,
                                       BigDecimal changeRate, BigDecimal volRatio,
                                       StockAnalysisContext context, double[] macdToday) {
        // 1. 量價爆發力得分 (權重 35%)：結合漲幅與放量倍數
        BigDecimal priceScore = changeRate.multiply(new BigDecimal("1000")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volScore = volRatio.multiply(new BigDecimal("33.3")).min(new BigDecimal("100")).max(BigDecimal.ZERO);
        BigDecimal volumePowerScore = priceScore.multiply(new BigDecimal("0.4")).add(volScore.multiply(new BigDecimal("0.6")));

        // 2. 籌碼面得分 (權重 25%)：基於獲利盤比例與單峰密集狀態
        BigDecimal chipScore = new BigDecimal("60.0");
        if (context != null && context.getProfitRatio() != null) {
            chipScore = context.getProfitRatio().multiply(new BigDecimal("100"));
            if (Boolean.TRUE.equals(context.getIsChipSinglePeak())) {
                chipScore = chipScore.add(new BigDecimal("10"));
            }
            chipScore = chipScore.min(new BigDecimal("100"));
        }

        // 3. 技術動能得分 (權重 20%)：基於 MACD 紅柱強度
        BigDecimal momentumScore = macdToday[2] > 0.05 ? new BigDecimal("100") : new BigDecimal("70");

        // 4. 形態品質得分 (權重 20%)：固定基礎形態分
        BigDecimal patternScore = new BigDecimal("80");

        // 加權綜合總得分計算
        BigDecimal totalScore = volumePowerScore.multiply(new BigDecimal("0.35"))
                .add(chipScore.multiply(new BigDecimal("0.25")))
                .add(momentumScore.multiply(new BigDecimal("0.20")))
                .add(patternScore.multiply(new BigDecimal("0.20")));

        builder.volumeScore(volumePowerScore)
                .chipScore(chipScore)
                .momentumScore(momentumScore)
                .patternScore(patternScore)
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

    /**
     * 計算 MACD 指標 (12, 26, 9)
     * @return 陣列格式 [DIF, DEA, HIST]
     */
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

    /**
     * 計算 KDJ 隨機指標 (9, 3, 3)
     * @return 陣列格式 [K, D, J]
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
        return new double[]{k, d, 3.0 * k - 2.0 * d};
    }

    /**
     * 計算 OBV 能量潮指標
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
     * 計算 RSI(N) 相對強弱指標
     */
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