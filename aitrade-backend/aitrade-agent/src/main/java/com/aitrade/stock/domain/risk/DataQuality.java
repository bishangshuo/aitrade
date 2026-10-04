package com.aitrade.stock.domain.risk;

import com.aitrade.stock.enums.DataLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataQuality {

    private DataLevel dataLevel;

    private int expectedPeriods;

    private int incomePeriods;

    private int balancePeriods;

    private int cashFlowPeriods;

    private int completePeriods;

    private BigDecimal incomeCoverage = BigDecimal.ZERO;

    private BigDecimal balanceCoverage = BigDecimal.ZERO;

    private BigDecimal cashFlowCoverage = BigDecimal.ZERO;

    private BigDecimal completeCoverage = BigDecimal.ZERO;

    private List<LocalDate> expectedReportDates = new ArrayList<>();

    private List<LocalDate> missingIncomePeriods = new ArrayList<>();

    private List<LocalDate> missingBalancePeriods = new ArrayList<>();

    private List<LocalDate> missingCashFlowPeriods = new ArrayList<>();

    private List<LocalDate> missingCompletePeriods = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();
}