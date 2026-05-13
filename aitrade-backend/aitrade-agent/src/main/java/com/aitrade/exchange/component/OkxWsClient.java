package com.aitrade.exchange.component;

import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.service.IKlineService;
import com.aitrade.exchange.task.CompensationTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OkxWsClient {

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private IKlineService klineService;

    @Autowired
    private CompensationTask compensationTask;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String URL = "wss://wspap.okx.com:8443/ws/v5/business";
    private static final long ONE_MINUTE_MS = 60000;
    private static final long MAX_RETRY_INTERVAL = 60000;

    private long retryInterval = 1000;
    private long lastTimestamp = 0;
    private volatile boolean isManualClose = false;
    private WebSocket webSocket;
    private ScheduledExecutorService heartbeatExecutor;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void start() {
        // 启动时先恢复数据
        compensationTask.fullRecoveryOnStartup();
        //重新计算MACD指标
        compensationTask.recalculateIndicatorsFromDB();
        // 建立WebSocket连接
        //connect();
    }

    private void connect() {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("WebSocket connected");
                isManualClose = false;
                retryInterval = 1000;

                // 订阅K线数据
                String subscribeMsg = """
                    {
                      "op": "subscribe",
                      "args": [
                        {
                          "channel": "candle1m",
                          "instId": "BTC-USDT"
                        }
                      ]
                    }
                    """;
                webSocket.send(subscribeMsg);

                // 启动心跳保活（每50秒发送一次ping）
                startHeartbeat(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    handleMessage(text);
                } catch (Exception e) {
                    log.error("消息处理异常", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                log.error("WebSocket error: {}", t.getMessage());
                if (!isManualClose) {
                    scheduleReconnect();
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                log.info("Closed: {}", reason);
                if (!isManualClose) {
                    scheduleReconnect();
                }
            }
        });
    }

    /**
     * 处理收到的实时消息
     * OKX推送的数据结构（索引从0开始）：
     * [0] ts: 开始时间（毫秒时间戳）
     * [1] o: 开盘价
     * [2] h: 最高价
     * [3] l: 最低价
     * [4] c: 收盘价
     * [5] vol: 交易量
     * [6] volCcy: 交易量（以币为单位）
     * [7] volCcyQuote: 交易量（以计价货币为单位）
     * [8] confirm: K线状态（0=未完结，1=已完结）
     */
    private void handleMessage(String text) {
        try {
            JsonNode root = mapper.readTree(text);

            if (!root.has("data") || root.get("data").size() == 0) {
                return;
            }

            JsonNode arr = root.get("data").get(0);
            long currentTimestamp = arr.get(0).asLong();
            String confirm = arr.get(8).asText();

            // 1. 时间戳连续性检测（检测数据缺失）
            if (lastTimestamp > 0) {
                long diff = currentTimestamp - lastTimestamp;
                if (diff > ONE_MINUTE_MS + 1000) {
                    long missingCount = diff / ONE_MINUTE_MS - 1;
                    log.warn("检测到 {} 条K线缺失！从 {} 到 {}",
                            missingCount, lastTimestamp, currentTimestamp);

                    // 异步触发补偿
                    final long startTime = lastTimestamp + ONE_MINUTE_MS;
                    final long endTime = currentTimestamp - ONE_MINUTE_MS;
                    CompletableFuture.runAsync(() ->
                            compensationTask.compensateRange(startTime));
                }
            }
            lastTimestamp = currentTimestamp;

            // 2. 构建Kline对象
            Kline k = new Kline();
            k.setSymbol("BTC-USDT");
            k.setOpenTime(arr.get(0).asLong());
            k.setOpen(new BigDecimal(arr.get(1).asText()));
            k.setHigh(new BigDecimal(arr.get(2).asText()));
            k.setLow(new BigDecimal(arr.get(3).asText()));
            k.setClose(new BigDecimal(arr.get(4).asText()));
            k.setVolume(new BigDecimal(arr.get(5).asText()));

            // 补充成交额字段
            if (arr.size() > 6) {
                k.setQuoteVolume(new BigDecimal(arr.get(6).asText()));
            }

            // 3. 判断是否最终确认
            boolean isFinal = "1".equals(confirm);
            k.setIsFinal(isFinal);

            // 4. 更新Redis中的最后接收时间戳
            redisTemplate.opsForValue().set(
                    "ws:lastTimestamp:BTC-USDT",
                    String.valueOf(currentTimestamp)
            );

            // 5. 处理K线数据 - 每次推送都调用process方法
            klineService.process(k, isFinal);

            log.debug("收到K线数据: time={}, close={}, confirm={}",
                    currentTimestamp, k.getClose(), confirm);

        } catch (Exception e) {
            log.error("解析WebSocket消息失败: {}", text, e);
        }
    }

    /**
     * 心跳保活机制（每50秒发送ping，防止代理服务器超时断开）
     */
    private void startHeartbeat(WebSocket ws) {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (ws != null) {
                String pingMsg = """
                    {
                      "op": "ping"
                    }
                    """;
                ws.send(pingMsg);
                log.debug("发送心跳Ping");
            }
        }, 50, 50, TimeUnit.SECONDS);
    }

    /**
     * 指数退避重连策略
     */
    private void scheduleReconnect() {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("将在 {}ms 后重连...", retryInterval);
                Thread.sleep(retryInterval);
                connect();
                // 重连成功后触发补偿
                compensationTask.compensateOnReconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // 重连失败，加倍间隔
                if (retryInterval < MAX_RETRY_INTERVAL) {
                    retryInterval = Math.min(retryInterval * 2, MAX_RETRY_INTERVAL);
                }
                scheduleReconnect();
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        isManualClose = true;
        if (webSocket != null) {
            webSocket.close(1000, "shutdown");
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
    }
}
