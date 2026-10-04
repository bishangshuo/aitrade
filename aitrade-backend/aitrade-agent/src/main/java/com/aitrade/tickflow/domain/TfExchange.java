package com.aitrade.tickflow.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TfExchange implements Serializable {
    private static final long serialVersionUID = 1L;

    private String exchange;
    private String region;
    private Long count;
}
