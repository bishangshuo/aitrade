package com.aitrade.exchange.service;

import com.aitrade.exchange.domain.Kline;

public interface IKlineService {
    void process(Kline k, boolean isFinal);
}
