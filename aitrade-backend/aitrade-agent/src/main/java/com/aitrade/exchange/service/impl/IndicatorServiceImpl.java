package com.aitrade.exchange.service.impl;

import com.aitrade.exchange.service.IIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IndicatorServiceImpl implements IIndicatorService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void updateMACD(String symbol, double close) {
        try {
            String key = "indicator:" + symbol;

            // 获取上一次的EMA值
            double ema12 = getDouble(key, "ema12");
            double ema26 = getDouble(key, "ema26");

            // 计算新的EMA
            double newEma12 = ema(ema12, close, 12);
            double newEma26 = ema(ema26, close, 26);

            // 计算MACD线
            double macd = newEma12 - newEma26;

            // 存储到Redis
            redisTemplate.opsForHash().put(key, "ema12", String.valueOf(newEma12));
            redisTemplate.opsForHash().put(key, "ema26", String.valueOf(newEma26));
            redisTemplate.opsForHash().put(key, "macd", String.valueOf(macd));

            log.trace("MACD更新: symbol={}, close={}, ema12={}, ema26={}, macd={}",
                    symbol, close, newEma12, newEma26, macd);
        } catch (Exception e) {
            log.error("MACD更新失败: symbol={}, close={}", symbol, close, e);
        }
    }

    private double ema(double prev, double price, int period) {
        double alpha = 2.0 / (period + 1);
        return alpha * price + (1 - alpha) * prev;
    }

    private double getDouble(String key, String field) {
        Object v = redisTemplate.opsForHash().get(key, field);
        if (v == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
