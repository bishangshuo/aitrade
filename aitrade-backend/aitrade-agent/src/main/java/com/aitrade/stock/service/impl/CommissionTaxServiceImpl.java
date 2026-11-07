package com.aitrade.stock.service.impl;

import com.aitrade.stock.enums.TradeType;
import com.aitrade.stock.service.ICommissionTaxService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CommissionTaxServiceImpl implements ICommissionTaxService {

    /**
     * 费率取常见的万2.5
     */
    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.00025);

    /**
     * 手续费计算
     * 法定上限为成交金额的3‰（千分之三），单笔不足5元按5元收取
     * @param tradeAmount
     * @return
     */
    @Override
    public BigDecimal getCommission(BigDecimal tradeAmount) {
        BigDecimal commission = tradeAmount.multiply(COMMISSION_RATE);
        if (commission.compareTo(BigDecimal.valueOf(5)) < 0) {
            return BigDecimal.valueOf(5);
        } else {
            return commission;
        }
    }

    /**
     * 过户费	成交金额的0.001%（万分之0.1）
     * @param tradeAmount
     * @return
     */
    @Override
    public BigDecimal getTransferFee(BigDecimal tradeAmount) {
        return tradeAmount.multiply(BigDecimal.valueOf(0.0001));
    }

    /**
     * 税费计算
     * 成交金额的0.05%（万分之五）
     * @param tradeAmount
     * @return
     */
    @Override
    public BigDecimal getTax(BigDecimal tradeAmount) {
        return tradeAmount.multiply(BigDecimal.valueOf(0.0005));
    }

    /**
     * 总费用计算
     * @param tradeType
     * @param tradeAmount
     * @return
     */
    @Override
    public BigDecimal getTotalFee(TradeType tradeType, BigDecimal tradeAmount) {
        if(tradeType == TradeType.BUY) {
            return getCommission(tradeAmount).add(getTransferFee(tradeAmount));
        } else if(tradeType == TradeType.SELL) {
            return getCommission(tradeAmount).add(getTransferFee(tradeAmount)).add(getTax(tradeAmount));
        }
        return BigDecimal.ZERO;
    }
}
