package com.aitrade.stock.service;

import com.aitrade.stock.domain.TfBacktestRecord;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IBacktestRunService {
    SseEmitter runBacktest(TfBacktestRecord backtestRecord);
}
