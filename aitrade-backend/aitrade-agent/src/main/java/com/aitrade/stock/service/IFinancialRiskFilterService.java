package com.aitrade.stock.service;

import com.aitrade.stock.domain.risk.FinancialRiskResult;

import java.time.LocalDate;

public interface IFinancialRiskFilterService {
    boolean isRisk(String secuCode);
    boolean isRisk(String secuCode, LocalDate asOfDate);

    boolean isEligibleForTechnicalSelection(String secuCode);

    boolean isEligibleForTechnicalSelection(
            String secuCode,
            LocalDate asOfDate
    );

    FinancialRiskResult evaluate(String secuCode);

    FinancialRiskResult evaluate(
            String secuCode,
            LocalDate asOfDate
    );
}
