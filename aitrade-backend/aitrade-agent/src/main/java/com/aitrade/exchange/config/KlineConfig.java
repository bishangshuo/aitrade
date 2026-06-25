package com.aitrade.exchange.config;

import com.aitrade.exchange.domain.KlineSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KlineConfig {
    @Value("${kline.time-length}")
    private long timeLength;
    @Value("${kline.kline-time}")
    private long klineTime ;
    @Value("${kline.kline-interval}")
    private String klineInterval ;
    @Value("${kline.candle-name}")
    private String candleName ;
    @Value("${kline.wave-length}")
    private int waveLength ;

    @Bean
    public KlineSettings klineSettings() {
        return new KlineSettings(timeLength, klineTime, klineInterval, candleName, waveLength);
    }
}
