package com.aitrade.tickflow.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TfKline implements Serializable {
    private static final long serialVersionUID = 1L;
    private String symbol;
    private String stockName;
    private Long timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private BigDecimal amount;
}
