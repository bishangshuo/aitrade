package com.aitrade.exchange.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class KlineSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private long timeLength;
    private long klineTime;
    private String klineInterval;
    private String candleName;
    private int waveLength;

    public KlineSettings(long timeLength, long klineTime, String klineInterval, String candleName, int waveLength) {
        this.timeLength = timeLength;
        this.klineTime = klineTime;
        this.klineInterval = klineInterval;
        this.candleName = candleName;
        this.waveLength = waveLength;
    }
}
