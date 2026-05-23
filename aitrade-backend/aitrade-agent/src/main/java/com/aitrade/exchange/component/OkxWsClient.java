package com.aitrade.exchange.component;

import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.domain.SymbolState;
import com.aitrade.exchange.event.SymbolChangeEvent;
import com.aitrade.exchange.handler.RecoveryHandler;
import com.aitrade.exchange.handler.WsConnectionOpenHandler;
import com.aitrade.exchange.service.IKlineService;
import com.aitrade.exchange.task.CompensationTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/**
 * OKX WebSocket 客户端 - 多交易对版本
 *
 * 核心改动：
 * 1. 单连接订阅多个交易对的 candle15m 频道
 * 2. 从消息的 arg.instId 字段提取交易对，路由到对应 SymbolState
 * 3. 监听 SymbolChangeEvent，动态 subscribe / unsubscribe
 * 4. 重连时自动重新订阅所有活跃交易对
 */
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
    @Autowired
    private SymbolManager symbolManager;

    @Value("${crypto.time-length}")
    private long timeLength;

    @Value("${crypto.kline}")
    private String klineInterval;

    @Value("${crypto.candle-name}")
    private String candleName;

    @Value("${crypto.kline-time}")
    private long klineTime;

    private static final String URL = "wss://wspap.okx.com:8443/ws/v5/business";
    private static final long MAX_RETRY_INTERVAL = 60000;
    private long retryInterval = 1000;
    private volatile boolean isManualClose = false;
    private WebSocket webSocket;
    private ScheduledExecutorService heartbeatExecutor;
    private final ObjectMapper mapper = new ObjectMapper();

    /** 每个交易对的运行时状态 */
    private final ConcurrentHashMap<String, SymbolState> symbolStateMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        // 1. 初始化默认交易对（如果Redis中没有的话）
        symbolManager.initDefaultsIfEmpty();

        // 2. 建立 WebSocket 连接
        connect(new WsConnectionOpenHandler() {
            @Override
            public void onOpen() {
                // 3. 恢复数据 & 重算指标（遍历所有活跃交易对）
                for (String symbol : symbolManager.getActiveSymbols()) {
                    getOrCreateSymbolState(symbol);
                    compensationTask.fullRecoveryOnStartup(symbol,new RecoveryHandler() {

                        @Override
                        public void onComplete(String symbol) {
                            //恢复完成才订阅
                            subscribeSymbol(symbol);
                            //并开始计算k线指标
                            compensationTask.recalculateIndicatorsFromDB(symbol);
                        }

                        @Override
                        public void onError(String symbol, Throwable t) {

                        }

                        @Override
                        public void onProgress(String symbol, int progress) {

                        }
                    });
                }
            }
        });

    }

    /**
     * 监听交易对变更事件，动态订阅/取消订阅
     */
    @EventListener
    public void onSymbolChange(SymbolChangeEvent event) {
        String symbol = event.getSymbol();
        switch (event.getAction()) {
            case ADD -> {
                getOrCreateSymbolState(symbol);
                // 新增交易对时触发一次全量恢复
                compensationTask.fullRecoveryOnStartup(symbol, new RecoveryHandler() {

                    @Override
                    public void onComplete(String symbol) {
                        //恢复完成才订阅
                        subscribeSymbol(symbol);
                        //并开始计算k线指标
                        compensationTask.recalculateIndicatorsFromDB(symbol);
                    }

                    @Override
                    public void onError(String symbol, Throwable t) {

                    }

                    @Override
                    public void onProgress(String symbol, int progress) {

                    }
                });

                log.info("动态订阅交易对: {}", symbol);
            }
            case REMOVE -> {
                unsubscribeSymbol(symbol);
                symbolStateMap.remove(symbol);
                log.info("动态取消订阅交易对: {}", symbol);
            }
        }
    }

    /**
     * 获取或创建交易对状态
     */
    private SymbolState getOrCreateSymbolState(String symbol) {
        return symbolStateMap.computeIfAbsent(symbol, SymbolState::new);
    }

    private void connect(WsConnectionOpenHandler onOpenHandler) {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                log.info("WebSocket connected");
                isManualClose = false;
                retryInterval = 1000;
                // 启动心跳保活
                startHeartbeat(webSocket);

                if(onOpenHandler != null) {
                    onOpenHandler.onOpen();
                }
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
     * 动态订阅单个交易对
     */
    private void subscribeSymbol(String symbol) {
        if (webSocket != null) {
            String subscribeMsg = String.format(
                    "{\"op\":\"subscribe\",\"args\":[{\"channel\":\"%s\",\"instId\":\"%s\"}]}",
                    candleName, symbol);
            webSocket.send(subscribeMsg);
            log.info("发送订阅请求: {}", symbol);
        }
    }

    /**
     * 动态取消订阅单个交易对
     */
    private void unsubscribeSymbol(String symbol) {
        if (webSocket != null) {
            String unsubMsg = String.format(
                    "{\"op\":\"unsubscribe\",\"args\":[{\"channel\":\"%s\",\"instId\":\"%s\"}]}",
                    candleName, symbol);
            webSocket.send(unsubMsg);
            log.info("发送取消订阅请求: {}", symbol);
        }
    }

    /**
     * 处理收到的实时消息
     *
     * OKX 推送数据结构：
     * {
     *   "arg": {"channel": "candleName", "instId": "BTC-USDT"},
     *   "data": [[ts, o, h, l, c, vol, volCcy, volCcyQuote, confirm], ...]
     * }
     */
    private void handleMessage(String text) {
        try {
            JsonNode root = mapper.readTree(text);

            // 跳过非数据消息（如订阅确认、pong等）
            if (!root.has("data") || root.get("data").size() == 0) {
                return;
            }

            // ★ 关键改动：从 arg.instId 提取交易对
            String symbol = null;
            if (root.has("arg") && root.get("arg").has("instId")) {
                symbol = root.get("arg").get("instId").asText();
            }
            if (symbol == null || !symbolManager.isActive(symbol)) {
                log.debug("忽略非活跃交易对消息: {}", symbol);
                return;
            }

            SymbolState state = getOrCreateSymbolState(symbol);
            JsonNode arr = root.get("data").get(0);
            long currentTimestamp = arr.get(0).asLong();
            String confirm = arr.get(8).asText();

            // 1. 时间戳连续性检测（检测数据缺失）
            if (state.getLastTimestamp() > 0) {
                long diff = currentTimestamp - state.getLastTimestamp();
                if (diff > klineTime + 1000) {
                    long missingCount = diff / klineTime - 1;
                    log.warn("[{}] 检测到 {} 条K线缺失！从 {} 到 {}",
                            symbol, missingCount, state.getLastTimestamp(), currentTimestamp);
                    final long startTime = state.getLastTimestamp() + klineTime;
                    final String finalSymbol = symbol;
                    CompletableFuture.runAsync(() ->
                            compensationTask.compensateRange(finalSymbol, startTime, null));
                }
            }
            state.setLastTimestamp(currentTimestamp);

            // 2. 构建 Kline 对象
            Kline k = new Kline();
            k.setSymbol(symbol);  // ★ 使用消息中的交易对，不再硬编码
            k.setOpenTime(arr.get(0).asLong());
            k.setOpen(new BigDecimal(arr.get(1).asText()));
            k.setHigh(new BigDecimal(arr.get(2).asText()));
            k.setLow(new BigDecimal(arr.get(3).asText()));
            k.setClose(new BigDecimal(arr.get(4).asText()));
            k.setVolume(new BigDecimal(arr.get(5).asText()));
            if (arr.size() > 6) {
                k.setQuoteVolume(new BigDecimal(arr.get(6).asText()));
            }

            // 3. 判断是否最终确认
            boolean isFinal = "1".equals(confirm);
            k.setIsFinal(isFinal);

            // 4. 更新 Redis 中的最后接收时间戳
            redisTemplate.opsForValue().set(
                    "ws:lastTimestamp:" + symbol,
                    String.valueOf(currentTimestamp)
            );

            // 5. 处理K线数据
            klineService.process(k, isFinal);

            log.debug("[{}] 收到K线数据: time={}, close={}, confirm={}",
                    symbol, currentTimestamp, k.getClose(), confirm);
        } catch (Exception e) {
            log.error("解析WebSocket消息失败: {}", text, e);
        }
    }

    /**
     * 心跳保活机制
     */
    private void startHeartbeat(WebSocket ws) {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (ws != null) {
                String pingMsg = "{\"op\":\"ping\"}";
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
                connect(new WsConnectionOpenHandler() {
                    @Override
                    public void onOpen() {
                        // 重连成功后触发所有活跃交易对的补偿
                        for (String symbol : symbolManager.getActiveSymbols()) {
                            compensationTask.compensateOnReconnect(symbol, new RecoveryHandler() {

                                @Override
                                public void onComplete(String symbol) {
                                    //恢复完成才订阅
                                    subscribeSymbol(symbol);
                                    //并开始计算k线指标
                                    compensationTask.recalculateIndicatorsFromDB(symbol);
                                }

                                @Override
                                public void onError(String symbol, Throwable t) {

                                }

                                @Override
                                public void onProgress(String symbol, int progress) {

                                }
                            });
                        }
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
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