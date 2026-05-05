package com.aitrade.exchange.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Kline implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 开盘时间 毫秒
     */
    private Long openTime;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 成交量
     */
    private BigDecimal volume;

    /**
     * 成交额
     */
    private BigDecimal quoteVolume;

    /**
     * 是否收盘
     */
    private Boolean isFinal;
}
