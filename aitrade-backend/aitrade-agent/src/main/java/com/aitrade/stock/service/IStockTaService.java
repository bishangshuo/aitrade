package com.aitrade.stock.service;

import com.aitrade.tickflow.domain.TfStock;

import java.time.LocalDate;
import java.util.List;

/**
 * A股技术分析选股服务类
 */
public interface IStockTaService {
    /**
     * 开始进行技术分析选股
     */
    void startTA();

    /**
     * 开始进行技术分析选股，指定回测日
     * @param date
     */
    void startTA(LocalDate date);
}
