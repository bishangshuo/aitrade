package com.aitrade.stock.service;

import com.aitrade.stock.enums.TradeType;

import java.math.BigDecimal;

public interface ICommissionTaxService {
    //交易手续费，双向收取
    BigDecimal getCommission(BigDecimal tradeAmount);

    //过户费，双向收取
    BigDecimal getTransferFee(BigDecimal tradeAmount);

    //印花税，只有卖出时才需要计算税
    BigDecimal getTax(BigDecimal tradeAmount);
    //总费用，包含手续费、过户费和印花税
    BigDecimal getTotalFee(TradeType tradeType, BigDecimal tradeAmount);
}
