package com.aitrade.rag.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Raptor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long maxCluster;
    private Long maxToken;
    private String prompt;
    private Long randomSeed;
    private BigDecimal threshold;
    private Boolean useRaptor;
}
