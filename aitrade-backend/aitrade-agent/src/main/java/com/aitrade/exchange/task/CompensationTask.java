package com.aitrade.exchange.task;

import com.aitrade.exchange.component.SymbolManager;
import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.repository.KlineRepository;
import com.aitrade.exchange.service.IIndicatorService;
import com.aitrade.exchange.service.impl.KlineServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 补偿任务 - 多交易对版本
 *
 * 核心改动：
 * 1. 所有方法接受 symbol 参数，不再硬编码
 * 2. 定时任务遍历所有活跃交易对
 * 3. compensateRange / compensateOnReconnect / fullRecoveryOnStartup 均 per-symbol
 * 4. recalculateIndicatorsFromDB per-symbol
 */
@Component
@Slf4j
public class CompensationTask {

    @Autowired
    private KlineRepository repo;
    @Autowired
    private OkHttpClient client;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private IIndicatorService indicatorService;
    @Autowired
    private KlineServiceImpl klineService;
    @Autowired
    private SymbolManager symbolManager;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final long ONE_MINUTE_MS = 60000;
    private static final int MAX_BATCH_SIZE = 300;

    /**
     * 定时获取最终确认数据（遍历所有活跃交易对）
     * 每分钟的第5秒执行
     */
    @Scheduled(cron = "5 * * * * *")
    public void finalizeLastMinuteKline() {
        Set<String> symbols = symbolManager.getActiveSymbols();
        if (symbols.isEmpty()) return;

        // 并行处理所有交易对
        for (String symbol : symbols) {
            CompletableFuture.runAsync(() -> finalizeLastMinuteKlineForSymbol(symbol));
        }
    }

    /**
     * 对单个交易对执行定时确认
     */
    private void finalizeLastMinuteKlineForSymbol(String symbol) {
        try {
            long now = System.currentTimeMillis();
            long lastMinuteStart = ((now / 60000) - 1) * 60000;

            String url = String.format(
                    "https://www.okx.com/api/v5/market/candles?instId=%s&bar=1m&after=%d&limit=1",
                    symbol, lastMinuteStart / 1000
            );

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) return;
                JsonNode root = mapper.readTree(response.body().string());
                if (!root.has("data") || root.get("data").size() == 0) return;

                JsonNode arr = root.get("data").get(0);
                Kline k = new Kline();
                k.setSymbol(symbol);
                k.setOpenTime(arr.get(0).asLong());
                k.setOpen(new BigDecimal(arr.get(1).asText()));
                k.setHigh(new BigDecimal(arr.get(2).asText()));
                k.setLow(new BigDecimal(arr.get(3).asText()));
                k.setClose(new BigDecimal(arr.get(4).asText()));
                k.setVolume(new BigDecimal(arr.get(5).asText()));
                if (arr.size() > 6) {
                    k.setQuoteVolume(new BigDecimal(arr.get(6).asText()));
                }
                k.setIsFinal(true);

                klineService.process(k, true);
                log.info("[{}] 定时任务确认K线: time={}, close={}", symbol, k.getOpenTime(), k.getClose());
            }
        } catch (Exception e) {
            log.error("[{}] 定时确认K线任务失败", symbol, e);
        }
    }

    /**
     * 重连成功后触发补偿（per-symbol）
     */
    public void compensateOnReconnect(String symbol) {
        try {
            String lastTimeStr = redisTemplate.opsForValue().get("ws:lastTimestamp:" + symbol);
            if (lastTimeStr == null) return;
            long lastTime = Long.parseLong(lastTimeStr);
            long now = System.currentTimeMillis();
            long diffMinutes = (now - lastTime) / ONE_MINUTE_MS;
            if (diffMinutes > 1) {
                log.info("[{}] 重连后检测到 {} 分钟数据缺失，开始补偿...", symbol, diffMinutes);
                compensateRange(symbol, lastTime);
            }
        } catch (Exception e) {
            log.error("[{}] 重连补偿失败", symbol, e);
        }
    }

    /**
     * 精准范围补偿（per-symbol）
     */
    public void compensateRange(String symbol, long startTime) {
        try {
            log.info("[{}] 开始精准补偿：从 {} 开始", symbol, new Timestamp(startTime));

            int fetchSize = MAX_BATCH_SIZE;
            long afterTime = startTime + fetchSize * ONE_MINUTE_MS;
            long now = System.currentTimeMillis();
            if (now < afterTime) {
                afterTime = now;
                fetchSize = (int) ((afterTime - startTime) / ONE_MINUTE_MS);
            }

            String url = String.format(
                    "https://www.okx.com/api/v5/market/history-candles?instId=%s&bar=1m&limit=%d&after=%d",
                    symbol, fetchSize, afterTime
            );

            List<Kline> klines = fetchKlinesFromHttp(url, symbol);
            if (klines.isEmpty()) {
                log.warn("[{}] 未获取到任何K线数据", symbol);
                return;
            }

            // 按时间正序排列
            klines.sort(Comparator.comparingLong(Kline::getOpenTime));

            log.info("[{}] 补偿到 {} 条K线数据，时间范围：{} ~ {}",
                    symbol, klines.size(),
                    new Timestamp(klines.get(0).getOpenTime()),
                    new Timestamp(klines.get(klines.size() - 1).getOpenTime()));

            klineService.processBatch(klines, true);

            Kline lastKline = klines.get(klines.size() - 1);
            long lastOpenTime = lastKline.getOpenTime();
            redisTemplate.opsForValue().set(
                    "ws:lastTimestamp:" + symbol,
                    String.valueOf(lastOpenTime)
            );

            // 继续以 lastOpenTime 为开始时间补偿
            compensateRange(symbol, lastOpenTime);
        } catch (Exception e) {
            log.error("[{}] 范围补偿失败", symbol, e);
        }
    }

    /**
     * 程序启动时全量恢复（per-symbol）
     */
    public void fullRecoveryOnStartup(String symbol) {
        try {
            log.info("[{}] ===== 开始启动数据恢复 =====", symbol);

            Long lastDbTime = repo.getLastOpenTime(symbol);
            long lastTime = lastDbTime != null ? lastDbTime : 0;

            String wsLastStr = redisTemplate.opsForValue().get("ws:lastTimestamp:" + symbol);
            long wsLastTime = wsLastStr != null ? Long.parseLong(wsLastStr) : 0;

            long recoveryStart = Math.max(lastTime, wsLastTime);
            long now = System.currentTimeMillis();

            if (recoveryStart == 0) {
                recoveryStart = now - 3600000;
            }

            if (now - recoveryStart > ONE_MINUTE_MS) {
                log.info("[{}] 需要恢复从 {} 到现在的数据", symbol, new Timestamp(recoveryStart));
                compensateRange(symbol, recoveryStart);
            }
        } catch (Exception e) {
            log.error("[{}] 启动数据恢复失败", symbol, e);
        }
    }

    /**
     * MACD指标重算（per-symbol）
     */
    public void recalculateIndicatorsFromDB(String symbol) {
        List<Kline> recentKlines = repo.findLatest(symbol, 2000);
        if (!recentKlines.isEmpty()) {
            recentKlines.sort(Comparator.comparingLong(Kline::getOpenTime));

            // 重置MACD状态
            String key = "indicator:" + symbol;
            redisTemplate.opsForHash().delete(key, "ema12", "ema26", "macd");

            // 重新计算
            for (Kline k : recentKlines) {
                indicatorService.updateMACD(symbol, k.getClose().doubleValue());
            }
            log.info("[{}] MACD指标重新计算完成，使用了 {} 条历史数据", symbol, recentKlines.size());
        }
    }

    /**
     * HTTP 获取K线数据（per-symbol）
     */
    private List<Kline> fetchKlinesFromHttp(String url, String symbol) throws IOException {
        log.debug("[{}] 请求K线数据: {}", symbol, url);
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.warn("[{}] 响应体为空", symbol);
                return Collections.emptyList();
            }
            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);

            String code = root.get("code").asText();
            if (!"0".equals(code)) {
                log.error("[{}] API请求失败: code={}, msg={}", symbol, code, root.get("msg").asText());
                return Collections.emptyList();
            }

            if (!root.has("data") || root.get("data").size() == 0) {
                log.debug("[{}] 无数据返回", symbol);
                return Collections.emptyList();
            }

            List<Kline> result = new ArrayList<>();
            JsonNode dataArray = root.get("data");
            for (JsonNode arr : dataArray) {
                try {
                    Kline k = new Kline();
                    k.setSymbol(symbol);
                    k.setOpenTime(arr.get(0).asLong());
                    k.setOpen(new BigDecimal(arr.get(1).asText()));
                    k.setHigh(new BigDecimal(arr.get(2).asText()));
                    k.setLow(new BigDecimal(arr.get(3).asText()));
                    k.setClose(new BigDecimal(arr.get(4).asText()));
                    k.setVolume(new BigDecimal(arr.get(5).asText()));
                    if (arr.size() > 6) {
                        k.setQuoteVolume(new BigDecimal(arr.get(6).asText()));
                    }
                    k.setIsFinal(true);
                    result.add(k);
                } catch (Exception e) {
                    log.warn("[{}] 解析K线数据失败: {}", symbol, arr, e);
                }
            }
            log.debug("[{}] 获取到 {} 条K线数据", symbol, result.size());
            return result;
        } catch (Exception e) {
            log.error("[{}] HTTP请求失败: {}", symbol, url, e);
            throw e;
        }
    }
}