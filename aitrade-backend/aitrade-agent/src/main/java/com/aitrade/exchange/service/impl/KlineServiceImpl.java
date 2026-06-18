package com.aitrade.exchange.service.impl;

import com.aitrade.exchange.component.SymbolState;
import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.repository.KlineRepository;
import com.aitrade.exchange.service.IKlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * K线处理服务 - 多交易对版本
 *
 * 核心改动：
 * 1. currentKlineTime → ConcurrentHashMap<String, Long>，每个交易对独立
 * 2. lastConfirmedKline → ConcurrentHashMap<String, Kline>，每个交易对独立
 * 3. confirmedCandles 去重 key 已包含 symbol，无需改动
 * 4. process() 方法中所有状态读写都通过 symbol 路由
 */
@Service
@Slf4j
public class KlineServiceImpl implements IKlineService {

    @Autowired
    private KlineRepository repo;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 只对已确认的K线做去重，防止重复写入数据库
    private final Set<String> confirmedCandles = ConcurrentHashMap.newKeySet();

    /** 每个交易对当前正在处理的K线时间戳 */
    private final ConcurrentHashMap<String, Long> currentKlineTimeMap = new ConcurrentHashMap<>();

    /** 每个交易对上一根已确认的K线数据 */
    private final ConcurrentHashMap<String, Kline> lastConfirmedKlineMap = new ConcurrentHashMap<>();

    private final Map<String, SymbolState> symbolStates = new ConcurrentHashMap<>();

    @Override
    public void processWebsocketKline(Kline k, boolean isFinal) {
        String symbol = k.getSymbol();
        long openTime = k.getOpenTime();

        // ========== 第一部分：即時流處理（每次推送都執行） ==========

        // 1. 更新 Redis 動態行情報告（保留 5 分鐘快取，提供前端或即時看板插針數據）
        String cacheKey = "kline:live:" + symbol + ":" + openTime;
        redisTemplate.opsForValue().set(
                cacheKey,
                String.format("%d,%s,%s,%s,%s,%s,%s",
                        openTime, k.getOpen(), k.getHigh(),
                        k.getLow(), k.getClose(), k.getVolume(), k.getQuoteVolume()),
                5, TimeUnit.MINUTES
        );

        // 2. 更新最新最新收盤價（供高頻秒級風控引擎動態比對止損線，無需過 TA4J）
        redisTemplate.opsForValue().set(
                "kline:current:" + symbol,
                String.valueOf(k.getClose()),
                1, TimeUnit.MINUTES
        );

        // 🚀【核心修正】移除原有的「if (k.getOpenTime() > currentKlineTime)」被動驅動邏輯
        // 讓第一部分回歸純粹的「即時流流數據清洗與快取維護」職責

        // ========== 第二部分：完結邊界處理（僅當 isFinal = true 時執行） ==========
        if (isFinal) {

            // 3. 完結數據精密去重防線（防止 WS 斷線重連推播重複的完結數據包）
            String confirmKey = symbol + ":" + openTime + ":confirmed";
            if (!confirmedCandles.add(confirmKey)) {
                log.debug("[{}] 該週期的收盤確認訊號已處理過，攔截重複推播: time={}", symbol, openTime);
                return;
            }

            // 記憶體輕量化維護：防止 7x24 小時 OOM。若超過閾值，採用更安全的清理策略
            if (confirmedCandles.size() > 5000) {
                // 實際生產中推薦使用 Guava Cache TTL，若用普通 Set 清理，建議留有緩衝，此處做簡單安全清理
                confirmedCandles.clear();
                confirmedCandles.add(confirmKey); // 把當前這個撈回來，防止清空瞬間被鑽空子
            }

            // 4. 數據落庫 TimescaleDB（保證底層歷史時序數據的絕對完整性）
            repo.upsert(k);

            // 5. 更新 Redis 中的最後落庫時間戳基準（供斷線重連時的 HTTP 補償任務比對）
            redisTemplate.opsForValue().set(
                    "db:lastTimestamp:" + symbol,
                    String.valueOf(openTime)
            );

            // 6. 更新記憶體快取基準
            lastConfirmedKlineMap.put(symbol, k);
            currentKlineTimeMap.put(symbol, openTime);

            log.info("[{}] ─── K線正式收盤 ─── 寫入數據庫: time={}, close={}, volume={}",
                    symbol, openTime, k.getClose(), k.getVolume());

            // 7. 【唯一驅動入口】在完結的瞬間，立刻點火驅動策略大腦（TA4J 餵入、指標計算、開平倉訊號）
            fireStrategySignal(k);
        }
    }

    /**
     * 从数据库加载历史K线数据到Ta4j数据结构
     * @param symbol
     */
    @Override
    public void loadKlinesToStateFromDb(String symbol) {
        List<Kline> klinesInDb = repo.findBySymbol(symbol);
        SymbolState state = getOrCreateState(symbol);
        state.loadHistoricalBars(klinesInDb);
    }

    private SymbolState getOrCreateState(String symbol) {
        return symbolStates.computeIfAbsent(symbol, s -> {
            SymbolState newState = new SymbolState(s);   // 使用你修改后的 SymbolState
            // 可选：在这里预加载最近的历史数据加速初始化
            return newState;
        });
    }

    /**
     * 触发策略信号（当一根K线确认结束时调用）
     */
    private void fireStrategySignal(Kline k) {
        String symbol = k.getSymbol();

        // 获取 SymbolState
        SymbolState state = getOrCreateState(symbol);   // 使用你之前添加的 getOrCreateState 方法

        if (state == null) {
            log.warn("[{}] SymbolState 未初始化，跳过策略计算", symbol);
            return;
        }

        // 更新 TA4J 数据并计算策略信号
        state.addBar(k, true);

        log.debug("[{}] 策略信号计算完成 - close={}", symbol, k.getClose());
    }


    /**
     * 批量处理补偿数据（用于补偿任务）
     */
    public void processBatch(List<Kline> klines) {
        if (klines.isEmpty()) return;

        String symbol = klines.get(0).getSymbol();
        SymbolState state = getOrCreateState(symbol);

        // 批量加载到 TA4J
        state.loadHistoricalBars(klines);

        for (Kline k : klines) {
            k.setIsFinal(true);

            String confirmKey = k.getSymbol() + ":" + k.getOpenTime() + ":confirmed";
            if (!confirmedCandles.add(confirmKey)) {
                continue;
            }

            repo.upsert(k);
        }
        if (!klines.isEmpty()) {
            log.info("批量处理 {} 条补偿数据", klines.size());
        }
    }

    /**
     * 获取当前最新K线数据
     */
    public Kline getCurrentKline(String symbol) {
        String cacheKey = "kline:current:" + symbol;
        String value = redisTemplate.opsForValue().get(cacheKey);
        if (value != null) {
            Kline k = new Kline();
            k.setSymbol(symbol);
            k.setClose(new BigDecimal(value));
            return k;
        }
        return null;
    }
}