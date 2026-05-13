package com.aitrade.exchange.task;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private final ObjectMapper mapper = new ObjectMapper();
    private static final long ONE_MINUTE_MS = 60000;
    private static final int MAX_BATCH_SIZE = 300;

    /**
     * 定时获取最终确认数据（作为WebSocket confirm=1的补充/备份）
     * 每分钟的第5秒执行，获取上一分钟的最终K线数据
     */
    @Scheduled(cron = "5 * * * * *")
    public void finalizeLastMinuteKline() {
        try {
            long now = System.currentTimeMillis();
            long lastMinuteStart = ((now / 60000) - 1) * 60000;

            String url = String.format(
                    "https://www.okx.com/api/v5/market/candles?instId=BTC-USDT&bar=1m&after=%d&limit=1",
                    lastMinuteStart / 1000
            );

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) return;

                JsonNode root = mapper.readTree(response.body().string());
                if (!root.has("data") || root.get("data").size() == 0) return;

                JsonNode arr = root.get("data").get(0);
                Kline k = new Kline();
                k.setSymbol("BTC-USDT");
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

                // 通过klineService处理（内部有去重逻辑）
                klineService.process(k, true);

                log.info("定时任务确认K线: time={}, close={}", k.getOpenTime(), k.getClose());
            }
        } catch (Exception e) {
            log.error("定时确认K线任务失败", e);
        }
    }

    /**
     * 重连成功后触发补偿
     */
    public void compensateOnReconnect() {
        try {
            String lastTimeStr = redisTemplate.opsForValue().get("ws:lastTimestamp:BTC-USDT");
            if (lastTimeStr == null) return;

            long lastTime = Long.parseLong(lastTimeStr);
            long now = System.currentTimeMillis();
            long diffMinutes = (now - lastTime) / ONE_MINUTE_MS;

            if (diffMinutes > 1) {
                log.info("重连后检测到 {} 分钟数据缺失，开始补偿...", diffMinutes);
                compensateRange(lastTime);
            }
        } catch (Exception e) {
            log.error("重连补偿失败", e);
        }
    }

    /**
     * 精准范围补偿
     */
    public void compensateRange(long startTime) {
        try {
            log.info("开始精准补偿：从 {} 开始", new Timestamp(startTime));

            // 使用历史K线接口（get_history_candlesticks可以获取更早的数据）
            // 使用after参数，after=startTime+MAX_BATCH_SIZE*60*1000
            int fetchSize = MAX_BATCH_SIZE;
            long afterTime = startTime + fetchSize * ONE_MINUTE_MS;
            long now = System.currentTimeMillis();
            if(now < afterTime) {
                afterTime = now;
                fetchSize = (int)((afterTime - startTime) / ONE_MINUTE_MS);
            }
            String url = String.format(
                    "https://www.okx.com/api/v5/market/history-candles?instId=BTC-USDT&bar=1m&limit=%d&after=%d",
                    fetchSize,
                    afterTime // 从此时间开始获取
            );

            //返回的klines数据是按时间新到旧排序
            List<Kline> klines = fetchKlinesFromHttp(url);
            if (klines.isEmpty()) {
                log.warn("未获取到任何K线数据");
                return;
            }
            // 按时间正序排列
            klines.sort(Comparator.comparingLong(Kline::getOpenTime));

            if (!klines.isEmpty()) {
                log.info("补偿到 {} 条K线数据，时间范围：{} ~ {}",
                        klines.size(),
                        new Timestamp(klines.get(0).getOpenTime()),
                        new Timestamp(klines.get(klines.size()-1).getOpenTime()));

                klineService.processBatch(klines, true);

                //第一个kline
                Kline lastKline = klines.get(klines.size()-1);
                //最后一个kline的时间
                long lastOpenTime = lastKline.getOpenTime();
                //跟新最后 时间
                redisTemplate.opsForValue().set(
                        "ws:lastTimestamp:BTC-USDT",
                        String.valueOf(lastOpenTime)
                );

                //继续以lastOpenTime为开始时间补偿
                compensateRange(lastOpenTime);
            }

        } catch (Exception e) {
            log.error("范围补偿失败", e);
        }
    }


    /**
     * 程序启动时全量恢复
     */
    public void fullRecoveryOnStartup() {
        try {
            log.info("===== 开始启动数据恢复 =====");

            Long lastDbTime = repo.getLastOpenTime("BTC-USDT");
            long lastTime = lastDbTime != null ? lastDbTime : 0;

            String wsLastStr = redisTemplate.opsForValue().get("ws:lastTimestamp:BTC-USDT");
            long wsLastTime = wsLastStr != null ? Long.parseLong(wsLastStr) : 0;

            long recoveryStart = Math.max(lastTime, wsLastTime);
            long now = System.currentTimeMillis();

            if (recoveryStart == 0) {
                recoveryStart = now - 3600000;
            }

            if (now - recoveryStart > ONE_MINUTE_MS) {
                log.info("需要恢复从 {} 到现在的数据", new Timestamp(recoveryStart));
                compensateRange(recoveryStart);
            }

            // 重新计算MACD指标
            //recalculateIndicatorsFromDB();
            //log.info("===== 启动数据恢复完成 =====");
        } catch (Exception e) {
            log.error("启动数据恢复失败", e);
        }
    }

    public void recalculateIndicatorsFromDB() {
        List<Kline> recentKlines = repo.findLatest("BTC-USDT", 2000);
        if (!recentKlines.isEmpty()) {
            recentKlines.sort(Comparator.comparingLong(Kline::getOpenTime));

            // 重置MACD状态
            String key = "indicator:BTC-USDT";
            redisTemplate.opsForHash().delete(key, "ema12", "ema26", "macd");

            // 重新计算
            for (Kline k : recentKlines) {
                indicatorService.updateMACD(k.getSymbol(), k.getClose().doubleValue());
            }
            log.info("MACD指标重新计算完成，使用了 {} 条历史数据", recentKlines.size());
        }
    }

    private List<Kline> fetchKlinesFromHttp(String url) throws IOException {
        log.debug("请求K线数据: {}", url);

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.warn("响应体为空");
                return Collections.emptyList();
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);

            // 检查API返回码
            String code = root.get("code").asText();
            if (!"0".equals(code)) {
                log.error("API请求失败: code={}, msg={}", code, root.get("msg").asText());
                return Collections.emptyList();
            }

            if (!root.has("data") || root.get("data").size() == 0) {
                log.debug("无数据返回");
                return Collections.emptyList();
            }

            List<Kline> result = new ArrayList<>();
            JsonNode dataArray = root.get("data");

            for (JsonNode arr : dataArray) {
                try {
                    Kline k = new Kline();
                    k.setSymbol("BTC-USDT");
                    k.setOpenTime(arr.get(0).asLong()); // ts
                    k.setOpen(new BigDecimal(arr.get(1).asText())); // o
                    k.setHigh(new BigDecimal(arr.get(2).asText())); // h
                    k.setLow(new BigDecimal(arr.get(3).asText())); // l
                    k.setClose(new BigDecimal(arr.get(4).asText())); // c
                    k.setVolume(new BigDecimal(arr.get(5).asText())); // vol
                    if (arr.size() > 6) {
                        k.setQuoteVolume(new BigDecimal(arr.get(6).asText())); // volCcy
                    }
                    k.setIsFinal(true); // HTTP获取的数据是最终确认的
                    result.add(k);
                } catch (Exception e) {
                    log.warn("解析K线数据失败: {}", arr, e);
                }
            }

            log.debug("获取到 {} 条K线数据", result.size());
            return result;
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", url, e);
            throw e;
        }
    }

}

