package com.aitrade.stock.service;

import com.aitrade.stock.domain.TfBacktestRecord;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;

public interface IBackTestService {
    SseEmitter backTest(TfBacktestRecord record);
}
