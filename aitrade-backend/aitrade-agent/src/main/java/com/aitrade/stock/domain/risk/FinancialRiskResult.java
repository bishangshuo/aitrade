package com.aitrade.stock.domain.risk;

import com.aitrade.stock.enums.DataLevel;
import com.aitrade.stock.enums.RiskLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class FinancialRiskResult {

    private String secuCode;

    private boolean financialRisk;

    private boolean eligibleForTechnicalSelection;

    private RiskLevel riskLevel;

    private BigDecimal riskScore;

    private DataLevel dataLevel;

    private int expectedPeriods;

    private int observedPeriods;

    private int incomePeriods;

    private int balancePeriods;

    private int cashFlowPeriods;

    private int completePeriods;

    private BigDecimal incomeCoverage;

    private BigDecimal balanceCoverage;

    private BigDecimal cashFlowCoverage;

    private BigDecimal completeCoverage;

    private LocalDate asOfDate;

    private List<LocalDate> missingPeriods = new ArrayList<>();

    private List<String> dataWarnings = new ArrayList<>();

    private List<String> riskReasons = new ArrayList<>();

    private Map<String, BigDecimal> dimensionScores = new LinkedHashMap<>();
}