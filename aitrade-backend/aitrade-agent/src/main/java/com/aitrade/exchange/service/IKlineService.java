package com.aitrade.exchange.service;

import com.aitrade.exchange.domain.Kline;

public interface IKlineService {
    void processWebsocketKline(Kline k, boolean isFinal);
    void loadKlinesToStateFromDb(String symbol);
}
