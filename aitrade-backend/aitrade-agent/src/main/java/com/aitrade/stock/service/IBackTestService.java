package com.aitrade.stock.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;

public interface IBackTestService {
    SseEmitter backTest(LocalDate fromDate, LocalDate toDate);
}
