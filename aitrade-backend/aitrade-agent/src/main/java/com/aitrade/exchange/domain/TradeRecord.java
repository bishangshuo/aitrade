package com.aitrade.exchange.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String symbol;

    /** ENTRY 或 EXIT */
    private String tradeType;

    private Long entryIndex;      // 日线 Bar 的索引
    private Long exitIndex;

    private BigDecimal entryPrice;
    private BigDecimal exitPrice;

    private BigDecimal amount;

    private Long entryTime;
    private Long exitTime;

    private String reason;

    private String remark;

    private Integer status;
    private Date createTime;
    private Date updateTime;
}