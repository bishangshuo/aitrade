package com.aitrade.stock.domain.risk;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class FinancialRow {

    private String secuCode;
    private LocalDate reportDate;
    private String reportType;
    private String reportDateName;
    private LocalDate noticeDate;
    private LocalDate updateDate;

    private Map<String, BigDecimal> values = new HashMap<>();

    public BigDecimal get(String column) {
        return values.get(column);
    }

    public boolean has(String column) {
        return values.containsKey(column)
                && values.get(column) != null;
    }
}
