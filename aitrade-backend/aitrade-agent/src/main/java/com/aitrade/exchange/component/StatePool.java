package com.aitrade.exchange.component;

import com.aitrade.exchange.config.RabbitMQConfig;
import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.domain.KlineSettings;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class StatePool {

    @Autowired
    private KlineSettings klineSettings;

    private ThreadPoolExecutor taskExecutor;

    private final Map<String, SymbolState> symbolStates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        int cpuCores = Runtime.getRuntime().availableProcessors();

        // 策略運算屬於 CPU 密集型，執行緒池大小不宜過大，通常為 核心數 到 核心數 * 2
        this.taskExecutor = new ThreadPoolExecutor(
                cpuCores,
                cpuCores * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000), // 緩衝佇列，防止爆記憶體
                new ThreadFactory() {
                    private final AtomicInteger idBuilder = new AtomicInteger(1);
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        return new Thread(r, "StatePool-Worker-" + idBuilder.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.DiscardOldestPolicy() // 飽和拒絕策略：行情追求實時，拋棄最舊事件
        );
    }

    private SymbolState getOrCreateState(String symbol) {
        return symbolStates.computeIfAbsent(symbol, s -> {
            SymbolState newState = new SymbolState(s, klineSettings);   // 使用你修改后的 SymbolState
            // 可选：在这里预加载最近的历史数据加速初始化
            return newState;
        });
    }

    public void loadHistoricalBars(String symbol, List<Kline> klines) {
        SymbolState state = getOrCreateState(symbol);
        if(state != null) {
            state.loadHistoricalBars(klines);
        }
    }

    public void addBar(String symbol, Kline kline, boolean isFinal) {
        // 获取 SymbolState
        SymbolState state = getOrCreateState(symbol);   // 使用你之前添加的 getOrCreateState 方法

        if (state == null) {
            log.warn("[{}] SymbolState 未初始化，跳过策略计算", symbol);
            return;
        }
        state.addBar(kline, true);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void onKlineMessage(Kline kline) {
        this.taskExecutor.execute(() -> {
            addBar(kline.getSymbol(), kline, true);
        });
    }
}
