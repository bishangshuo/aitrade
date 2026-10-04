package com.aitrade.stock.domain.risk;

import com.aitrade.stock.enums.RiskDimension;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DimensionResult {

    private RiskDimension dimension;

    private boolean available;

    private BigDecimal score = BigDecimal.ZERO;

    private BigDecimal weight;

    private String reason;

    public DimensionResult(
            RiskDimension dimension,
            boolean available,
            BigDecimal score,
            BigDecimal weight,
            String reason
    ) {
        this.dimension = dimension;
        this.available = available;
        this.score = score == null ? BigDecimal.ZERO : score;
        this.weight = weight == null ? BigDecimal.ZERO : weight;
        this.reason = reason;
    }
}
