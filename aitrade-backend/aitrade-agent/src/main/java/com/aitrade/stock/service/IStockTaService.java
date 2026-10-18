package com.aitrade.stock.service;

import com.aitrade.stock.handler.DayTaResultListener;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A股技术分析选股服务类
 */
public interface IStockTaService {
    /**
     * 开始进行技术分析选股
     * @param isRunning
     */
    void startTA(AtomicBoolean isRunning, DayTaResultListener listener);

    /**
     * 开始进行技术分析选股，指定回测日
     * @param date
     * @param isRunning
     */
    void startTA(LocalDate date, AtomicBoolean isRunning, DayTaResultListener listener);
}
