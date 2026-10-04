package com.aitrade.tickflow.domain;

import lombok.Data;

import java.io.Serializable;

/*
{
    "symbol": "588950.SH",
    "exchange": "SH",
    "code": "588950",
    "name": "科创50ETF景顺",
    "region": "CN",
    "type": "etf"
}
 */
@Data
public class TfStock implements Serializable {
    private String symbol;
    private String exchange;
    private String code;
    private String name;
    private String region;
    private String type;
    private TfStockExt ext;
}
