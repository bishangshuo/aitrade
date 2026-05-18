package com.aitrade.exchange.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class SymbolState implements Serializable {
    /** 交易对，如 BTC-USDT */
    private String symbol;

    /** WebSocket 最后收到的时间戳（用于缺失检测） */
    private volatile long lastTimestamp = 0;

    /** 当前正在处理的K线时间戳（用于判断是否进入新K线周期） */
    private volatile long currentKlineTime = 0;

    /** 上一根已确认的K线数据（用于触发策略信号） */
    private volatile Kline lastConfirmedKline = null;

    public SymbolState(String symbol) {
        this.symbol = symbol;
    }
}
