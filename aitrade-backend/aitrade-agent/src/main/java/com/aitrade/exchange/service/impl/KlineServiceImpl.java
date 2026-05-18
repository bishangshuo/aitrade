package com.aitrade.exchange.service.impl;

import com.aitrade.exchange.domain.Kline;
import com.aitrade.exchange.repository.KlineRepository;
import com.aitrade.exchange.service.IIndicatorService;
import com.aitrade.exchange.service.IKlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
    private IIndicatorService indicatorService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 只对已确认的K线做去重，防止重复写入数据库
    private final Set<String> confirmedCandles = ConcurrentHashMap.newKeySet();

    /** 每个交易对当前正在处理的K线时间戳 */
    private final ConcurrentHashMap<String, Long> currentKlineTimeMap = new ConcurrentHashMap<>();

    /** 每个交易对上一根已确认的K线数据 */
    private final ConcurrentHashMap<String, Kline> lastConfirmedKlineMap = new ConcurrentHashMap<>();

    @Override
    public void process(Kline k, boolean isFinal) {
        String symbol = k.getSymbol();

        // ========== 第一部分：实时更新 - 每次推送都执行 ==========

        // 1. 每次推送都更新MACD指标（实时性保障）
        indicatorService.updateMACD(symbol, k.getClose().doubleValue());

        // 2. 更新Redis缓存（覆盖旧值，保留最新数据）
        String cacheKey = "kline:live:" + symbol + ":" + k.getOpenTime();
        redisTemplate.opsForValue().set(
                cacheKey,
                String.format("%d,%s,%s,%s,%s,%s,%s",
                        k.getOpenTime(), k.getOpen(), k.getHigh(),
                        k.getLow(), k.getClose(), k.getVolume(), k.getQuoteVolume()),
                5, TimeUnit.MINUTES
        );

        // 3. 检测是否进入新K线周期（per-symbol）
        long currentKlineTime = currentKlineTimeMap.getOrDefault(symbol, 0L);
        if (k.getOpenTime() > currentKlineTime) {
            log.debug("[{}] 进入新K线周期: {}", symbol, k.getOpenTime());
            // 如果上一根K线已确认，触发策略信号
            Kline lastConfirmed = lastConfirmedKlineMap.get(symbol);
            if (lastConfirmed != null) {
                fireStrategySignal(lastConfirmed);
            }
            currentKlineTimeMap.put(symbol, k.getOpenTime());
        }

        // 4. 更新最新K线缓存（用于快速获取当前价格）
        redisTemplate.opsForValue().set(
                "kline:current:" + symbol,
                String.valueOf(k.getClose()),
                1, TimeUnit.MINUTES
        );

        // ========== 第二部分：最终确认处理 - 仅confirmed=1时执行 ==========
        if (isFinal) {
            // 5. 对最终确认数据做去重
            String confirmKey = symbol + ":" + k.getOpenTime() + ":confirmed";
            if (!confirmedCandles.add(confirmKey)) {
                log.debug("[{}] 已确认的K线已处理过，跳过: time={}", symbol, k.getOpenTime());
                return;
            }

            // 限制去重缓存大小，防止内存溢出
            if (confirmedCandles.size() > 10000) {
                confirmedCandles.clear();
            }

            // 6. 写入数据库
            repo.upsert(k);

            // 7. 记录最后一次确认的K线时间戳
            redisTemplate.opsForValue().set(
                    "db:lastTimestamp:" + symbol,
                    String.valueOf(k.getOpenTime())
            );

            // 8. 保存为上一根已确认K线（per-symbol）
            lastConfirmedKlineMap.put(symbol, k);

            log.info("[{}] 最终确认K线写入数据库: time={}, close={}, open={}, high={}, low={}, volume={}",
                    symbol, k.getOpenTime(), k.getClose(), k.getOpen(),
                    k.getHigh(), k.getLow(), k.getVolume());
        }
    }

    /**
     * 触发策略信号（当一根K线确认结束时调用）
     */
    private void fireStrategySignal(Kline k) {
        log.info("[{}] K线{}确认结束，触发策略信号计算，close={}",
                k.getSymbol(), k.getOpenTime(), k.getClose());
        // 在这里添加你的策略逻辑
    }

    /**
     * 批量处理补偿数据（用于补偿任务）
     */
    public void processBatch(List<Kline> klines, boolean recalculateMACD) {
        for (Kline k : klines) {
            k.setIsFinal(true);

            String confirmKey = k.getSymbol() + ":" + k.getOpenTime() + ":confirmed";
            if (!confirmedCandles.add(confirmKey)) {
                continue;
            }

            repo.upsert(k);

            if (recalculateMACD) {
                indicatorService.updateMACD(k.getSymbol(), k.getClose().doubleValue());
            }
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