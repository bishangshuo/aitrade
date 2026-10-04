package com.aitrade.stock.service;

import com.aitrade.tickflow.domain.TfStock;

import java.util.List;

/**
 * A股技术分析选股服务类
 */
public interface IStockTaService {
    /**
     * 开始进行技术分析选股
     */
    void startTA();
}
