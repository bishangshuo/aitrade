package com.aitrade.stock.handler;

import com.aitrade.tickflow.domain.TfStock;

public interface StockTaCompleteListener {
    void complete(TfStock stock, boolean success);
}
