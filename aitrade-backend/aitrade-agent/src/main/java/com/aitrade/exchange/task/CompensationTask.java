package com.aitrade.exchange.task;

import com.aitrade.exchange.component.SymbolManager;
import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.handler.RecoveryHandler;
import com.aitrade.exchange.repository.KlineRepository;
import com.aitrade.exchange.service.impl.KlineServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    private KlineServiceImpl klineService;
    @Autowired
    private SymbolManager symbolManager;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_BATCH_SIZE = 300;

    @Value("${crypto.time-length}")
    private long timeLength;

    @Value("${crypto.kline}")
    private String klineInterval;

    @Value("${crypto.kline-time}")
    private long klineTime;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 重连成功后触发补偿（per-symbol）
     */
    public void compensateOnReconnect(String symbol, RecoveryHandler handler) {
        try {
            String lastTimeStr = redisTemplate.opsForValue().get("ws:lastTimestamp:" + symbol);
            if (lastTimeStr == null) return;
            long lastTime = Long.parseLong(lastTimeStr);
            long now = System.currentTimeMillis();
            long diffMinutes = (now - lastTime) / klineTime;
            if (diffMinutes > 15) {
                log.info("[{}] 重连后检测到 {} 分钟数据缺失，开始补偿...", symbol, diffMinutes);
                compensateRange(symbol, lastTime, handler);
            }
        } catch (Exception e) {
            log.error("[{}] 重连补偿失败", symbol, e);
        }
    }

    /**
     * 精准范围补偿（per-symbol）
     */
    public void compensateRange(String symbol, long startTime, RecoveryHandler handler) {
        try {
            log.info("[{}] 开始精准补偿：从 {} 开始", symbol, new Timestamp(startTime));

            int fetchSize = MAX_BATCH_SIZE;
            long afterTime = startTime + fetchSize * klineTime;
            long now = System.currentTimeMillis();
            if (now < afterTime) {
                afterTime = now;
                fetchSize = (int) ((afterTime - startTime) / klineTime);
            }

            String url = String.format(
                    "https://www.okx.com/api/v5/market/history-candles?instId=%s&bar=%s&limit=%d&after=%d",
                    symbol, klineInterval,  fetchSize, afterTime
            );

            List<Kline> klines = fetchKlinesFromHttp(url, symbol);
            if (klines.isEmpty()) {
                log.warn("[{}] 未获取到任何K线数据", symbol);

                //有可能币种是后面才上的，前面没有数据，则需要移动开始时间到下一个时间点接续
                long nextStartTime = startTime + klineTime * fetchSize;
                long nextStartTime15 = nextStartTime / klineTime;
                long now15 = now / klineTime;

                if(nextStartTime15 < now15) {
                    compensateRange(symbol, nextStartTime, handler);
                } else {
                    if (handler != null) {
                        handler.onComplete(symbol);
                    }
                }
                return;
            }

            // 按时间正序排列
            klines.sort(Comparator.comparingLong(Kline::getOpenTime));

            log.info("[{}] 补偿到 {} 条K线数据，时间范围：{} ~ {}",
                    symbol, klines.size(),
                    new Timestamp(klines.get(0).getOpenTime()),
                    new Timestamp(klines.get(klines.size() - 1).getOpenTime()));

            klineService.processBatch(klines);

            Kline lastKline = klines.get(klines.size() - 1);
            long lastOpenTime = lastKline.getOpenTime();
            redisTemplate.opsForValue().set(
                    "ws:lastTimestamp:" + symbol,
                    String.valueOf(lastOpenTime)
            );

            // 继续以 lastOpenTime 为开始时间补偿
            // 线程停止500ms后继续执行
            Thread.sleep(500);
            compensateRange(symbol, lastOpenTime, handler);
        } catch (Exception e) {
            log.error("[{}] 范围补偿失败", symbol, e);
            if(handler != null) {
                handler.onError(symbol, e);
            }
        }
    }

    /**
     * 程序启动时全量恢复（per-symbol）
     */
    public void fullRecoveryOnStartup(String symbol, RecoveryHandler handler) {
        try {
            log.info("[{}] ===== 开始启动数据恢复 =====", symbol);

            Long lastDbTime = repo.getLastOpenTime(symbol);
            long lastTime = lastDbTime != null ? lastDbTime : 0;

            String wsLastStr = redisTemplate.opsForValue().get("ws:lastTimestamp:" + symbol);
            long wsLastTime = wsLastStr != null ? Long.parseLong(wsLastStr) : 0;

            long recoveryStart = Math.max(lastTime, wsLastTime);
            long now = System.currentTimeMillis();

            if (recoveryStart == 0) {
                recoveryStart = now - timeLength;
            }

            if (now - recoveryStart > klineTime) {
                log.info("[{}] 需要恢复从 {} 到现在的数据", symbol, new Timestamp(recoveryStart));
                compensateRange(symbol, recoveryStart, handler);
            } else {
                if(handler != null) {
                    handler.onComplete(symbol);
                    log.info("[{}] 数据恢复 + TA4J 初始化完成", symbol);
                }
            }
        } catch (Exception e) {
            log.error("[{}] 启动数据恢复失败", symbol, e);
            if(handler != null) {
                handler.onError(symbol, e);
            }
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