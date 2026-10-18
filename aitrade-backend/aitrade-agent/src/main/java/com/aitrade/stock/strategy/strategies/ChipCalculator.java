package com.aitrade.stock.strategy.strategies;

import com.aitrade.stock.strategy.domain.StockAnalysisContext;
import com.aitrade.tickflow.domain.TfKline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 基於歷史日 K 線推算的籌碼分佈 (CYQ) 計算器
 */
public class ChipCalculator {

    // 價格區間切分桶數 (例如將最高價與最低價之間切分為 100 個區間)
    private static final int PRICE_BINS_COUNT = 100;

    /**
     * 計算籌碼指標並填充 StockAnalysisContext
     *
     * @param klines 最近 120~250 天的日 K 線列表 (按時間升序)
     * @return 包含 profitRatio 與 isChipSinglePeak 的 Context
     */
    public static StockAnalysisContext calculateContextFromKlines(List<TfKline> klines) {
        if (klines == null || klines.size() < 60) {
            return StockAnalysisContext.builder()
                    .profitRatio(BigDecimal.ZERO)
                    .isChipSinglePeak(false)
                    .build();
        }

        int lastIdx = klines.size() - 1;
        TfKline today = klines.get(lastIdx);
        BigDecimal currentClose = today.getClose();

        // 1. 取得近 120 個交易日的最高價與最低價 (作為籌碼計算的總價格區間)
        int historyWindow = Math.min(klines.size(), 120);
        int startIdx = klines.size() - historyWindow;

        BigDecimal minPrice = klines.get(startIdx).getLow();
        BigDecimal maxPrice = klines.get(startIdx).getHigh();

        for (int i = startIdx; i <= lastIdx; i++) {
            if (klines.get(i).getLow().compareTo(minPrice) < 0) minPrice = klines.get(i).getLow();
            if (klines.get(i).getHigh().compareTo(maxPrice) > 0) maxPrice = klines.get(i).getHigh();
        }

        double minP = minPrice.doubleValue();
        double maxP = maxPrice.doubleValue();
        double priceRange = maxP - minP;

        if (priceRange <= 0) {
            return StockAnalysisContext.builder()
                    .profitRatio(BigDecimal.ONE)
                    .isChipSinglePeak(true)
                    .build();
        }

        // 2. 初始化籌碼分佈桶 (Price Bins)
        double[] chipBins = new double[PRICE_BINS_COUNT];
        double binWidth = priceRange / PRICE_BINS_COUNT;

        double totalChipWeight = 0.0;
        double profitChipWeight = 0.0;

        // 3. 模擬籌碼衰減與分佈 (從舊 K 線到新 K 線)
        // 衰減模型：當日換手率越高，歷史籌碼保留率越低
        for (int i = startIdx; i <= lastIdx; i++) {
            TfKline kline = klines.get(i);
            double high = kline.getHigh().doubleValue();
            double low = kline.getLow().doubleValue();
            double vol = kline.getVolume().doubleValue();

            // 簡化換手率估算：用當日成交量佔窗口期平均成交量的比例模擬
            // （若 TfKline 實體內有 turnOverRate 欄位，可直接替換為 kline.getTurnOverRate().doubleValue()）
            double dayWeight = vol;

            // 將當日成交量均勻分佈在當日的高低價區間內
            int lowBin = (int) Math.max(0, Math.floor((low - minP) / binWidth));
            int highBin = (int) Math.min(PRICE_BINS_COUNT - 1, Math.floor((high - minP) / binWidth));

            int binsCovered = highBin - lowBin + 1;
            double chipPerBin = dayWeight / binsCovered;

            // 歷史籌碼衰減 (假設每日籌碼保留係數 0.985，越近期的 K 線權重越高)
            double decayFactor = Math.pow(0.985, lastIdx - i);

            for (int b = lowBin; b <= highBin; b++) {
                double addedChip = chipPerBin * decayFactor;
                chipBins[b] += addedChip;
            }
        }

        // 4. 統計總籌碼量與獲利籌碼量 (收盤價以下的籌碼)
        int currentCloseBin = (int) Math.min(PRICE_BINS_COUNT - 1, Math.max(0, Math.floor((currentClose.doubleValue() - minP) / binWidth)));

        for (int b = 0; b < PRICE_BINS_COUNT; b++) {
            totalChipWeight += chipBins[b];
            if (b <= currentCloseBin) {
                profitChipWeight += chipBins[b];
            }
        }

        // 5. 計算獲利籌碼比例 (profitRatio)
        double profitRatioVal = (totalChipWeight > 0) ? (profitChipWeight / totalChipWeight) : 0.0;
        BigDecimal profitRatio = BigDecimal.valueOf(profitRatioVal).setScale(4, RoundingMode.HALF_UP);

        // 6. 計算籌碼單峰密集度 (isChipSinglePeak)
        // 判定標準：尋找最大籌碼峰，若最高峰及其相鄰 15% 價格區域集中了全場 60% 以上的籌碼，即為單峰密集
        boolean isSinglePeak = checkIsSinglePeak(chipBins, totalChipWeight);

        return StockAnalysisContext.builder()
                .profitRatio(profitRatio)
                .isChipSinglePeak(isSinglePeak)
                .build();
    }

    /**
     * 判定籌碼是否形成單峰密集
     */
    private static boolean checkIsSinglePeak(double[] chipBins, double totalChipWeight) {
        if (totalChipWeight <= 0) return false;

        // 1. 找出籌碼最多的峰值桶 (Peak Bin)
        int maxBin = 0;
        double maxVolume = 0.0;

        for (int i = 0; i < chipBins.length; i++) {
            if (chipBins[i] > maxVolume) {
                maxVolume = chipBins[i];
                maxBin = i;
            }
        }

        // 2. 統計主峰上下各 7 個桶（共約 15% 的價格寬度）累積的籌碼量
        int window = 7;
        int startBin = Math.max(0, maxBin - window);
        int endBin = Math.min(chipBins.length - 1, maxBin + window);

        double peakClusterVolume = 0.0;
        for (int i = startBin; i <= endBin; i++) {
            peakClusterVolume += chipBins[i];
        }

        // 3. 若主峰區間集結了超過 58% 的籌碼，且無次要大峰，則判定為單峰密集
        double concentrationRatio = peakClusterVolume / totalChipWeight;
        return concentrationRatio >= 0.58;
    }
}
