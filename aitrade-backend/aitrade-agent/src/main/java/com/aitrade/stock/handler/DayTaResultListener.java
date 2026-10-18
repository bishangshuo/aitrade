package com.aitrade.stock.handler;

import com.aitrade.stock.domain.StockPickDiagnostic;
import com.aitrade.tickflow.domain.TfStock;

public interface DayTaResultListener {
    //当天所有股票TA计算完成
    void onComplete();

    //当天 个股选股结果
    void stockSelectResult(TfStock stock, StockPickDiagnostic diagnostic);
}
