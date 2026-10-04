package com.aitrade.tickflow.domain;

import lombok.Data;

import java.io.Serializable;

/*
{
    "type": "cn_equity",
    "listing_date": "2025-01-24",
    "total_shares": 719729700.0,
    "float_shares": 719729700.0,
    "tick_size": 0.001,
    "limit_up": 2.041,
    "limit_down": 1.361
}
*/
@Data
public class TfStockExt implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String listingDate;
    private Double totalShares;
    private Double floatShares;
    private Double tickSize;
    private Double limitUp;
    private Double limitDown;
}
