package com.aitrade.stock.domain.risk;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FinancialData {

    private List<FinancialRow> income = new ArrayList<>();

    private List<FinancialRow> balance = new ArrayList<>();

    private List<FinancialRow> cashFlow = new ArrayList<>();

    private Map<LocalDate, FinancialRow> incomeMap = new HashMap<>();

    private Map<LocalDate, FinancialRow> balanceMap = new HashMap<>();

    private Map<LocalDate, FinancialRow> cashFlowMap = new HashMap<>();
}
