package com.aitrade.stock.handler;

import com.aitrade.tickflow.domain.TfStock;

public interface StockTaHandler {
    void complete(TfStock stock, boolean success);
}
