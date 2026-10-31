package com.aitrade.risk.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class RiskReport implements Serializable {
    private static final long serialVersionUID = 1L;
    private Boolean passed;
    private String reason;
}
