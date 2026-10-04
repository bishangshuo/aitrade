package com.aitrade.stock.service.impl;

import com.aitrade.stock.domain.risk.*;
import com.aitrade.stock.enums.ReportPeriodType;
import com.aitrade.stock.enums.RiskDimension;
import com.aitrade.stock.enums.RiskLevel;
import com.aitrade.stock.enums.DataLevel;
import com.aitrade.stock.service.IFinancialRiskFilterService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 财务风险过滤服务
 *
 * 核心原则：
 *
 * 1. income_statement / balance_sheet / cash_flow_statement 分开加载
 * 2. 不使用三表 INNER JOIN 作为财务风险判断前提
 * 3. 数据缺失 != 财务风险
 * 4. 只有实际财务指标异常才进入 financialRisk
 * 5. REPORT_DATE 才是财务报告期
 * 6. NOTICE_DATE 用于历史可见性截断，UPDATE_DATE 仅作为同一披露时点的版本排序辅助字段
 * 7. 同比必须使用相同 REPORT_TYPE / REPORT_DATE_NAME
 * 8. 缺少同比基期时跳过同比指标，不把缺失数据当 0
 * 9. 财务风险评分按实际可计算维度动态归一化
 * 10. 数据严重不足时返回 UNKNOWN，而不是强行判风险
 * 11. 历史回测必须按 NOTICE_DATE 截止日期过滤，禁止使用未来才披露的财报
 *
 * 当前适配：
 * PostgreSQL / TimescaleDB
 *
 * 表：
 * public.income_statement
 * public.balance_sheet
 * public.cash_flow_statement
 */
@Service
public class FinancialRiskFilterServiceImpl implements IFinancialRiskFilterService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 最近检查多少个报告期。
     *
     * 你的数据目前从：
     * 2023-06-30
     * 到
     * 2026-06-30
     *
     * 实际最近 12 个报告期：
     * 2023-09-30
     * 2023-12-31
     * 2024-03-31
     * 2024-06-30
     * 2024-09-30
     * 2024-12-31
     * 2025-03-31
     * 2025-06-30
     * 2025-09-30
     * 2025-12-31
     * 2026-03-31
     * 2026-06-30
     */
    private static final int EXPECTED_PERIOD_COUNT = 12;

    /**
     * 至少有多少个报告期才认为具备一定历史分析能力。
     */
    private static final int LIMITED_HISTORY_PERIODS = 4;

    /**
     * 少于该数量，历史分析基本不可用。
     */
    private static final int INSUFFICIENT_PERIODS = 2;

    /**
     * 至少有多少风险权重可计算，才允许输出确定性的财务风险。
     *
     * 注意：
     * 这里是“可计算权重”，不是三张表必须全部存在。
     */
    private static final BigDecimal MIN_AVAILABLE_WEIGHT =
            new BigDecimal("0.50");

    /**
     * 财务风险总分 >= 60：风险
     */
    private static final BigDecimal RISK_SCORE_THRESHOLD =
            new BigDecimal("60");

    /**
     * 财务风险等级。
     */


    /**
     * 数据质量等级。
     */


    /**
     * 报告期类型。
     */


    /**
     * 风险维度。
     */


    /**
     * 单个财务报告。
     */


    /**
     * 三张表的财务数据。
     */


    /**
     * 数据质量。
     */


    /**
     * 风险维度结果。
     */


    /**
     * 最终结果。
     */


    public FinancialRiskFilterServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * ============================================================
     * 对外入口
     * ============================================================
     */

    /**
     * 判断是否存在实际财务风险。
     *
     * 注意：
     * 数据缺失不会直接返回 true。
     */
    @Override
    public boolean isRisk(String secuCode) {

        return isRisk(secuCode, LocalDate.now());
    }

    /**
     * 按指定历史日期判断财务风险。
     *
     * 回测时必须使用该方法，避免将 asOfDate 之后才披露的财报带入历史判断。
     */
    @Override
    public boolean isRisk(String secuCode, LocalDate asOfDate) {

        FinancialRiskResult result = evaluate(secuCode, asOfDate);

        return result.isFinancialRisk();
    }

    /**
     * 判断是否允许进入后续技术面选股。
     *
     * 与 isRisk() 有本质区别：
     *
     * financialRisk = 真正的财务风险
     *
     * eligibleForTechnicalSelection = 财务数据是否足够支持技术选股
     */
    @Override
    public boolean isEligibleForTechnicalSelection(String secuCode) {

        return isEligibleForTechnicalSelection(secuCode, LocalDate.now());
    }

    /**
     * 按指定历史日期判断是否允许进入技术面选股。
     */
    @Override
    public boolean isEligibleForTechnicalSelection(
            String secuCode,
            LocalDate asOfDate
    ) {

        FinancialRiskResult result =
                evaluate(secuCode, asOfDate);

        return result.isEligibleForTechnicalSelection();
    }

    /**
     * 完整分析。
     */
    @Override
    public FinancialRiskResult evaluate(String secuCode) {

        return evaluate(secuCode, LocalDate.now());
    }

    /**
     * 按指定历史日期进行完整财务风险分析。
     *
     * 核心规则：
     * 1. REPORT_DATE 表示财务报告期；
     * 2. NOTICE_DATE 表示该报告何时对市场可见；
     * 3. 回测时只允许使用 NOTICE_DATE 在 asOfDate 当日及之前的报告；
     * 4. 同一 REPORT_DATE 如果存在多个历史版本，只取截至 asOfDate 已知的最新版本。
     */
    @Override
    public FinancialRiskResult evaluate(
            String secuCode,
            LocalDate asOfDate
    ) {

        if (!StringUtils.hasText(secuCode)) {
            throw new IllegalArgumentException("secuCode不能为空");
        }

        if (asOfDate == null) {
            throw new IllegalArgumentException("asOfDate不能为空");
        }

        secuCode = secuCode.trim().toUpperCase();

        FinancialData data =
                loadFinancialData(secuCode, asOfDate);

        DataQuality quality = analyzeDataQuality(data);

        FinancialRiskResult result = new FinancialRiskResult();

        result.setAsOfDate(asOfDate);

        result.setSecuCode(secuCode);

        result.setDataLevel(quality.getDataLevel());

        result.setExpectedPeriods(quality.getExpectedPeriods());

        result.setIncomePeriods(quality.getIncomePeriods());

        result.setBalancePeriods(quality.getBalancePeriods());

        result.setCashFlowPeriods(quality.getCashFlowPeriods());

        result.setCompletePeriods(quality.getCompletePeriods());

        result.setObservedPeriods(Math.max(
                quality.getIncomePeriods(),
                Math.max(
                        quality.getBalancePeriods(),
                        quality.getCashFlowPeriods()
                )
        ));

        result.setIncomeCoverage(quality.getIncomeCoverage());

        result.setBalanceCoverage(quality.getBalanceCoverage());

        result.setCashFlowCoverage(quality.getCashFlowCoverage());

        result.setCompleteCoverage(quality.getCompleteCoverage());

        result.getDataWarnings().addAll(quality.getWarnings());

        result.getMissingPeriods().addAll(
                unionMissingPeriods(quality)
        );

        /*
         * 数据完全不存在。
         */
        if (result.getObservedPeriods() == 0) {

            result.setFinancialRisk(false);

            result.setEligibleForTechnicalSelection(false);

            result.setRiskLevel(RiskLevel.UNKNOWN);

            result.setRiskScore(BigDecimal.ZERO);

            result.getDataWarnings().add("三张财务表均无数据");

            return result;
        }

        /*
         * 财务风险分析。
         */
        List<DimensionResult> dimensions = new ArrayList<>();

        dimensions.add(
                evaluateProfitability(data)
        );

        dimensions.add(
                evaluateCashFlow(data)
        );

        dimensions.add(
                evaluateSolvency(data)
        );

        dimensions.add(
                evaluateAssetQuality(data)
        );

        dimensions.add(
                evaluateProfitQuality(data)
        );

        /*
         * 保存维度评分。
         */
        for (DimensionResult dimension : dimensions) {

            if (dimension.isAvailable()) {

                result.getDimensionScores().put(
                        dimension.getDimension().name(),
                        scale(dimension.getScore())
                );

                if (StringUtils.hasText(dimension.getReason())) {
                    result.getRiskReasons().add(
                            dimension.getDimension().name() + ": "
                                    + dimension.getReason()
                    );
                }
            }
        }

        /*
         * 计算动态风险分。
         */
        BigDecimal availableWeight = dimensions.stream()
                .filter(d -> d.isAvailable())
                .map(d -> d.getWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal riskScore =
                calculateRiskScore(dimensions);

        result.setRiskScore(scale(riskScore));

        /*
         * 风险等级。
         */
        result.setRiskLevel(resolveRiskLevel(
                        riskScore,
                        availableWeight
                ));

        /*
         * financialRisk：
         *
         * 数据不足：
         * 不判风险。
         *
         * 有足够维度：
         * riskScore >= 60 判定风险。
         */
        result.setFinancialRisk(availableWeight.compareTo(MIN_AVAILABLE_WEIGHT) >= 0
                        && riskScore.compareTo(RISK_SCORE_THRESHOLD) >= 0);

        /*
         * 技术选股资格：
         *
         * 1. 不能存在 HIGH 财务风险
         * 2. 至少有一定历史
         * 3. 至少有一张核心财务表
         *
         * 数据缺失严重时，不进入技术选股。
         */
        result.setEligibleForTechnicalSelection(resolveTechnicalSelectionEligibility(
                        result,
                        quality
                ));

        return result;
    }

    /**
     * ============================================================
     * 数据加载
     * ============================================================
     */

    private FinancialData loadFinancialData(
            String secuCode,
            LocalDate asOfDate
    ) {

        FinancialData data = new FinancialData();

        data.setIncome(loadIncomeStatement(secuCode, asOfDate));

        data.setBalance(loadBalanceSheet(secuCode, asOfDate));

        data.setCashFlow(loadCashFlowStatement(secuCode, asOfDate));

        data.setIncomeMap(toMap(data.getIncome()));

        data.setBalanceMap(toMap(data.getBalance()));

        data.setCashFlowMap(toMap(data.getCashFlow()));

        return data;
    }

    /**
     * 加载利润表。
     */
    private List<FinancialRow> loadIncomeStatement(
            String secuCode,
            LocalDate asOfDate
    ) {

        String sql =
                "SELECT " +
                        "\"SECUCODE\", " +
                        "\"REPORT_DATE\", " +
                        "\"REPORT_TYPE\", " +
                        "\"REPORT_DATE_NAME\", " +
                        "\"NOTICE_DATE\", " +
                        "\"UPDATE_DATE\", " +
                        "\"TOTAL_OPERATE_INCOME\", " +
                        "\"OPERATE_INCOME\", " +
                        "\"OPERATE_PROFIT\", " +
                        "\"TOTAL_PROFIT\", " +
                        "\"NETPROFIT\", " +
                        "\"PARENT_NETPROFIT\", " +
                        "\"DEDUCT_PARENT_NETPROFIT\" " +
                "FROM ( " +
                    "SELECT *, " +
                    "ROW_NUMBER() OVER ( " +
                        "PARTITION BY \"SECUCODE\", \"REPORT_DATE\" " +
                        "ORDER BY \"NOTICE_DATE\" DESC NULLS LAST, " +
                                 "\"UPDATE_DATE\" DESC NULLS LAST " +
                    ") AS rn " +
                    "FROM \"public\".\"income_statement\" " +
                    "WHERE \"SECUCODE\" = ? " +
                        "AND \"NOTICE_DATE\" IS NOT NULL " +
                        "AND \"NOTICE_DATE\" < ? " +
                ") t " +
                "WHERE rn = 1 " +
                "ORDER BY \"REPORT_DATE\"";

        return jdbcTemplate.query(
                sql,
                this::mapIncomeRow,
                secuCode,
                Timestamp.valueOf(asOfDate.plusDays(1).atStartOfDay())
        );
    }

    /**
     * 加载资产负债表。
     */
    private List<FinancialRow> loadBalanceSheet(
            String secuCode,
            LocalDate asOfDate
    ) {

        String sql =
                "SELECT " +
                        "\"SECUCODE\", " +
                        "\"REPORT_DATE\", " +
                        "\"REPORT_TYPE\", " +
                        "\"REPORT_DATE_NAME\", " +
                        "\"NOTICE_DATE\", " +
                        "\"UPDATE_DATE\", " +
                        "\"MONETARYFUNDS\", " +
                        "\"TOTAL_CURRENT_ASSETS\", " +
                        "\"TOTAL_NONCURRENT_ASSETS\", " +
                        "\"TOTAL_ASSETS\", " +
                        "\"TOTAL_CURRENT_LIAB\", " +
                        "\"TOTAL_NONCURRENT_LIAB\", " +
                        "\"TOTAL_LIABILITIES\", " +
                        "\"TOTAL_EQUITY\", " +
                        "\"TOTAL_LIAB_EQUITY\", " +
                        "\"ACCOUNTS_RECE\", " +
                        "\"INVENTORY\" " +
                "FROM ( " +
                    "SELECT *, " +
                    "ROW_NUMBER() OVER ( " +
                        "PARTITION BY \"SECUCODE\", \"REPORT_DATE\" " +
                        "ORDER BY \"NOTICE_DATE\" DESC NULLS LAST, " +
                                 "\"UPDATE_DATE\" DESC NULLS LAST " +
                    ") AS rn " +
                    "FROM \"public\".\"balance_sheet\" " +
                    "WHERE \"SECUCODE\" = ? " +
                        "AND \"NOTICE_DATE\" IS NOT NULL " +
                        "AND \"NOTICE_DATE\" < ? " +
                ") t " +
                "WHERE rn = 1 " +
                "ORDER BY \"REPORT_DATE\"";

        return jdbcTemplate.query(
                sql,
                this::mapBalanceRow,
                secuCode,
                Timestamp.valueOf(asOfDate.plusDays(1).atStartOfDay())
        );
    }

    /**
     * 加载现金流量表。
     */
    private List<FinancialRow> loadCashFlowStatement(
            String secuCode,
            LocalDate asOfDate
    ) {

        String sql =
                "SELECT " +
                        "\"SECUCODE\", " +
                        "\"REPORT_DATE\", " +
                        "\"REPORT_TYPE\", " +
                        "\"REPORT_DATE_NAME\", " +
                        "\"NOTICE_DATE\", " +
                        "\"UPDATE_DATE\", " +
                        "\"NETCASH_OPERATE\", " +
                        "\"NETCASH_INVEST\", " +
                        "\"NETCASH_FINANCE\", " +
                        "\"CCE_ADD\", " +
                        "\"BEGIN_CCE\", " +
                        "\"END_CCE\", " +
                        "\"OPINION_TYPE\", " +
                        "\"OSOPINION_TYPE\" " +
                "FROM ( " +
                    "SELECT *, " +
                    "ROW_NUMBER() OVER ( " +
                        "PARTITION BY \"SECUCODE\", \"REPORT_DATE\" " +
                        "ORDER BY \"NOTICE_DATE\" DESC NULLS LAST, " +
                                 "\"UPDATE_DATE\" DESC NULLS LAST " +
                    ") AS rn " +
                    "FROM \"public\".\"cash_flow_statement\" " +
                    "WHERE \"SECUCODE\" = ? " +
                        "AND \"NOTICE_DATE\" IS NOT NULL " +
                        "AND \"NOTICE_DATE\" < ? " +
                ") t " +
                "WHERE rn = 1 " +
                "ORDER BY \"REPORT_DATE\"";

        return jdbcTemplate.query(
                sql,
                this::mapCashFlowRow,
                secuCode,
                Timestamp.valueOf(asOfDate.plusDays(1).atStartOfDay())
        );
    }

    /**
     * ============================================================
     * RowMapper
     * ============================================================
     */

    private FinancialRow mapIncomeRow(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {

        FinancialRow row = baseRow(rs);

        put(row, "TOTAL_OPERATE_INCOME", rs, "TOTAL_OPERATE_INCOME");

        put(row, "OPERATE_INCOME", rs, "OPERATE_INCOME");

        put(row, "OPERATE_PROFIT", rs, "OPERATE_PROFIT");

        put(row, "TOTAL_PROFIT", rs, "TOTAL_PROFIT");

        put(row, "NETPROFIT", rs, "NETPROFIT");

        put(row, "PARENT_NETPROFIT", rs, "PARENT_NETPROFIT");

        put(
                row,
                "DEDUCT_PARENT_NETPROFIT",
                rs,
                "DEDUCT_PARENT_NETPROFIT"
        );

        return row;
    }

    private FinancialRow mapBalanceRow(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {

        FinancialRow row = baseRow(rs);

        put(row, "MONETARYFUNDS", rs, "MONETARYFUNDS");

        put(
                row,
                "TOTAL_CURRENT_ASSETS",
                rs,
                "TOTAL_CURRENT_ASSETS"
        );

        put(
                row,
                "TOTAL_NONCURRENT_ASSETS",
                rs,
                "TOTAL_NONCURRENT_ASSETS"
        );

        put(
                row,
                "TOTAL_ASSETS",
                rs,
                "TOTAL_ASSETS"
        );

        put(
                row,
                "TOTAL_CURRENT_LIAB",
                rs,
                "TOTAL_CURRENT_LIAB"
        );

        put(
                row,
                "TOTAL_NONCURRENT_LIAB",
                rs,
                "TOTAL_NONCURRENT_LIAB"
        );

        put(
                row,
                "TOTAL_LIABILITIES",
                rs,
                "TOTAL_LIABILITIES"
        );

        put(
                row,
                "TOTAL_EQUITY",
                rs,
                "TOTAL_EQUITY"
        );

        put(
                row,
                "TOTAL_LIAB_EQUITY",
                rs,
                "TOTAL_LIAB_EQUITY"
        );

        put(row, "ACCOUNTS_RECE", rs, "ACCOUNTS_RECE");

        put(row, "INVENTORY", rs, "INVENTORY");

        return row;
    }

    private FinancialRow mapCashFlowRow(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {

        FinancialRow row = baseRow(rs);

        put(
                row,
                "NETCASH_OPERATE",
                rs,
                "NETCASH_OPERATE"
        );

        put(
                row,
                "NETCASH_INVEST",
                rs,
                "NETCASH_INVEST"
        );

        put(
                row,
                "NETCASH_FINANCE",
                rs,
                "NETCASH_FINANCE"
        );

        put(
                row,
                "CCE_ADD",
                rs,
                "CCE_ADD"
        );

        put(
                row,
                "BEGIN_CCE",
                rs,
                "BEGIN_CCE"
        );

        put(
                row,
                "END_CCE",
                rs,
                "END_CCE"
        );

        return row;
    }

    private FinancialRow baseRow(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        FinancialRow row = new FinancialRow();

        row.setSecuCode(rs.getString("SECUCODE"));

        Timestamp reportTimestamp =
                rs.getTimestamp("REPORT_DATE");

        if (reportTimestamp != null) {

            row.setReportDate(reportTimestamp
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        }

        row.setReportType(rs.getString("REPORT_TYPE"));

        row.setReportDateName(rs.getString("REPORT_DATE_NAME"));

        Timestamp noticeTimestamp =
                rs.getTimestamp("NOTICE_DATE");

        if (noticeTimestamp != null) {

            row.setNoticeDate(noticeTimestamp
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        }

        Timestamp updateTimestamp =
                rs.getTimestamp("UPDATE_DATE");

        if (updateTimestamp != null) {

            row.setUpdateDate(updateTimestamp
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        }

        return row;
    }

    private void put(
            FinancialRow row,
            String key,
            java.sql.ResultSet rs,
            String column
    ) throws java.sql.SQLException {

        BigDecimal value = rs.getBigDecimal(column);

        if (value != null) {
            row.getValues().put(key, value);
        }
    }

    private Map<LocalDate, FinancialRow> toMap(
            List<FinancialRow> rows
    ) {

        return rows.stream()
                .filter(r -> r.getReportDate() != null)
                .collect(
                        Collectors.toMap(
                                r -> r.getReportDate(),
                                r -> r,
                                (a, b) -> a
                        )
                );
    }

    /**
     * ============================================================
     * 数据质量
     * ============================================================
     */

    private DataQuality analyzeDataQuality(
            FinancialData data
    ) {

        DataQuality quality = new DataQuality();

        /*
         * 以三张表中的最大 REPORT_DATE 为当前财务数据终点。
         */
        LocalDate latestDate = latestReportDate(data);

        if (latestDate == null) {

            quality.setDataLevel(DataLevel.INSUFFICIENT);

            quality.setExpectedPeriods(0);

            return quality;
        }

        /*
         * 取最近 12 个真实报告期。
         *
         * 不使用：
         *
         * latestDate.minusYears(3)
         *
         * 因为：
         *
         * “三年”
         * !=
         * “12个财务报告期”
         */
        quality.setExpectedReportDates(loadExpectedReportDates(latestDate));

        quality.setExpectedPeriods(quality.getExpectedReportDates().size());

        Set<LocalDate> incomeDates =
                data.getIncomeMap().keySet();

        Set<LocalDate> balanceDates =
                data.getBalanceMap().keySet();

        Set<LocalDate> cashDates =
                data.getCashFlowMap().keySet();

        for (LocalDate date :
                quality.getExpectedReportDates()) {

            if (!incomeDates.contains(date)) {
                quality.getMissingIncomePeriods().add(date);
            }

            if (!balanceDates.contains(date)) {
                quality.getMissingBalancePeriods().add(date);
            }

            if (!cashDates.contains(date)) {
                quality.getMissingCashFlowPeriods().add(date);
            }

            if (incomeDates.contains(date)
                    && balanceDates.contains(date)
                    && cashDates.contains(date)) {

                /*
                 * 三张表均存在。
                 */
            } else {

                quality.getMissingCompletePeriods().add(date);
            }
        }

        quality.setIncomePeriods(quality.getExpectedPeriods()
                        - quality.getMissingIncomePeriods().size());

        quality.setBalancePeriods(quality.getExpectedPeriods()
                        - quality.getMissingBalancePeriods().size());

        quality.setCashFlowPeriods(quality.getExpectedPeriods()
                        - quality.getMissingCashFlowPeriods().size());

        quality.setCompletePeriods(quality.getExpectedPeriods()
                        - quality.getMissingCompletePeriods().size());

        quality.setIncomeCoverage(coverage(
                        quality.getIncomePeriods(),
                        quality.getExpectedPeriods()
                ));

        quality.setBalanceCoverage(coverage(
                        quality.getBalancePeriods(),
                        quality.getExpectedPeriods()
                ));

        quality.setCashFlowCoverage(coverage(
                        quality.getCashFlowPeriods(),
                        quality.getExpectedPeriods()
                ));

        quality.setCompleteCoverage(coverage(
                        quality.getCompletePeriods(),
                        quality.getExpectedPeriods()
                ));

        /*
         * 判断数据质量。
         */
        int maxPeriods =
                Math.max(
                        quality.getIncomePeriods(),
                        Math.max(
                                quality.getBalancePeriods(),
                                quality.getCashFlowPeriods()
                        )
                );

        int minPeriods =
                Math.min(
                        quality.getIncomePeriods(),
                        Math.min(
                                quality.getBalancePeriods(),
                                quality.getCashFlowPeriods()
                        )
                );

        if (maxPeriods < INSUFFICIENT_PERIODS) {

            quality.setDataLevel(DataLevel.INSUFFICIENT);

            quality.getWarnings().add(
                    "三张财务表有效历史均不足"
            );

        } else if (maxPeriods < LIMITED_HISTORY_PERIODS) {

            quality.setDataLevel(DataLevel.LIMITED_HISTORY);

            quality.getWarnings().add(
                    "财务历史较短，长期趋势指标可信度有限"
            );

        } else if (quality.getCompletePeriods() == quality.getExpectedPeriods()) {

            quality.setDataLevel(DataLevel.FULL);

        } else if (minPeriods == 0
                && maxPeriods >= LIMITED_HISTORY_PERIODS) {

            /*
             * 例如：
             *
             * income = 12
             * balance = 0
             * cash = 12
             *
             * 这不是财务风险。
             *
             * 这是数据结构性缺失。
             */
            quality.setDataLevel(DataLevel.DATA_ANOMALY);

            quality.getWarnings().add(
                    "至少一张财务报表在目标期间完全缺失"
            );

        } else {

            quality.setDataLevel(DataLevel.DATA_GAP);

            quality.getWarnings().add(
                    "三张财务报表存在部分报告期缺失"
            );
        }

        if (!quality.getMissingIncomePeriods().isEmpty()) {

            quality.getWarnings().add(
                    "利润表缺失报告期："
                            + quality.getMissingIncomePeriods()
            );
        }

        if (!quality.getMissingBalancePeriods().isEmpty()) {

            quality.getWarnings().add(
                    "资产负债表缺失报告期："
                            + quality.getMissingBalancePeriods()
            );
        }

        if (!quality.getMissingCashFlowPeriods().isEmpty()) {

            quality.getWarnings().add(
                    "现金流量表缺失报告期："
                            + quality.getMissingCashFlowPeriods()
            );
        }

        return quality;
    }

    /**
     * 从数据库实际存在的报告期中选最近12个。
     */
    private List<LocalDate> loadExpectedReportDates(
            LocalDate latestDate
    ) {

        String sql =
                "SELECT DISTINCT \"REPORT_DATE\"::date " +
                "FROM ( " +
                    "SELECT \"REPORT_DATE\" " +
                    "FROM \"public\".\"income_statement\" " +

                    "UNION " +

                    "SELECT \"REPORT_DATE\" " +
                    "FROM \"public\".\"balance_sheet\" " +

                    "UNION " +

                    "SELECT \"REPORT_DATE\" " +
                    "FROM \"public\".\"cash_flow_statement\" " +
                ") t " +
                "WHERE \"REPORT_DATE\"::date <= ? " +
                "ORDER BY \"REPORT_DATE\" DESC " +
                "LIMIT ?";

        List<LocalDate> dates =
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) ->
                                rs.getDate(1)
                                        .toLocalDate(),
                        java.sql.Date.valueOf(latestDate),
                        EXPECTED_PERIOD_COUNT
                );

        Collections.sort(dates);

        return dates;
    }

    private LocalDate latestReportDate(
            FinancialData data
    ) {

        return StreamMax(
                data.getIncome(),
                data.getBalance(),
                data.getCashFlow()
        );
    }

    private LocalDate StreamMax(
            List<FinancialRow> income,
            List<FinancialRow> balance,
            List<FinancialRow> cashFlow
    ) {

        return Arrays.asList(
                        income,
                        balance,
                        cashFlow
                )
                .stream()
                .flatMap(Collection::stream)
                .map(r -> r.getReportDate())
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private BigDecimal coverage(
            int actual,
            int expected
    ) {

        if (expected <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal
                .valueOf(actual)
                .divide(
                        BigDecimal.valueOf(expected),
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private List<LocalDate> unionMissingPeriods(
            DataQuality quality
    ) {

        Set<LocalDate> result =
                new TreeSet<>();

        result.addAll(
                quality.getMissingIncomePeriods()
        );

        result.addAll(
                quality.getMissingBalancePeriods()
        );

        result.addAll(
                quality.getMissingCashFlowPeriods()
        );

        return new ArrayList<>(result);
    }

    /**
     * ============================================================
     * 盈利能力
     * ============================================================
     */

    private DimensionResult evaluateProfitability(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.25");

        List<FinancialRow> rows =
                data.getIncome().stream()
                        .filter(r ->
                                r.getReportDate() != null
                        )
                        .sorted(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .collect(Collectors.toList());

        if (rows.isEmpty()) {

            return unavailable(
                    RiskDimension.PROFITABILITY,
                    weight,
                    "利润表无数据"
            );
        }

        FinancialRow latest =
                rows.get(rows.size() - 1);

        BigDecimal netProfit =
                first(
                        latest,
                        "PARENT_NETPROFIT",
                        "NETPROFIT"
                );

        BigDecimal revenue =
                first(
                        latest,
                        "TOTAL_OPERATE_INCOME",
                        "OPERATE_INCOME"
                );

        if (netProfit == null
                && revenue == null) {

            return unavailable(
                    RiskDimension.PROFITABILITY,
                    weight,
                    "利润表关键盈利字段缺失"
            );
        }

        BigDecimal score =
                BigDecimal.ZERO;

        List<String> reasons =
                new ArrayList<>();

        /*
         * 当前净利润 < 0
         */
        if (netProfit != null
                && netProfit.compareTo(BigDecimal.ZERO) < 0) {

            score =
                    score.add(
                            new BigDecimal("40")
                    );

            reasons.add("当前归母净利润为负");
        }

        /*
         * 营业收入 <= 0
         */
        if (revenue != null
                && revenue.compareTo(BigDecimal.ZERO) <= 0) {

            score =
                    score.add(
                            new BigDecimal("30")
                    );

            reasons.add("当前营业收入非正");
        }

        /*
         * 连续亏损。
         *
         * 只检查实际存在的数据。
         * 缺数据直接跳过。
         */
        int consecutiveLoss =
                consecutiveNegativeNetProfit(rows);

        if (consecutiveLoss >= 2) {

            score =
                    score.add(
                            new BigDecimal("20")
                    );

            reasons.add(
                    "连续"
                            + consecutiveLoss
                            + "个报告期归母净利润为负"
            );
        }

        /*
         * 同比恶化。
         *
         * 只使用相同报告期类型。
         */
        if (netProfit != null) {

            FinancialRow yoyBase =
                    findYoYPeriod(
                            latest,
                            rows
                    );

            BigDecimal baseProfit =
                    yoyBase == null
                            ? null
                            : first(
                                    yoyBase,
                                    "PARENT_NETPROFIT",
                                    "NETPROFIT"
                            );

            if (baseProfit != null
                    && baseProfit.compareTo(BigDecimal.ZERO) > 0
                    && netProfit.compareTo(BigDecimal.ZERO) < 0) {

                score =
                        score.add(
                                new BigDecimal("20")
                        );

                reasons.add(
                        "归母净利润同比由正转负"
                );
            }
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }

        return available(
                RiskDimension.PROFITABILITY,
                score,
                weight,
                String.join("；", reasons)
        );
    }

    private int consecutiveNegativeNetProfit(
            List<FinancialRow> rows
    ) {

        /*
         * 最多检查最近几个实际存在的报告。
         */
        int count = 0;

        for (int i = rows.size() - 1;
             i >= 0;
             i--) {

            BigDecimal profit =
                    first(
                            rows.get(i),
                            "PARENT_NETPROFIT",
                            "NETPROFIT"
                    );

            if (profit == null) {
                continue;
            }

            if (profit.compareTo(BigDecimal.ZERO) < 0) {

                count++;

            } else {

                break;
            }

            if (count >= 4) {
                break;
            }
        }

        return count;
    }

    /**
     * ============================================================
     * 经营现金流
     * ============================================================
     */

    private DimensionResult evaluateCashFlow(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.25");

        List<FinancialRow> rows =
                data.getCashFlow().stream()
                        .sorted(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .collect(Collectors.toList());

        if (rows.isEmpty()) {

            return unavailable(
                    RiskDimension.CASH_FLOW,
                    weight,
                    "现金流量表无数据"
            );
        }

        FinancialRow latest =
                rows.get(rows.size() - 1);

        BigDecimal ocf =
                latest.get(
                        "NETCASH_OPERATE"
                );

        if (ocf == null) {

            return unavailable(
                    RiskDimension.CASH_FLOW,
                    weight,
                    "经营活动现金流净额缺失"
            );
        }

        BigDecimal score =
                BigDecimal.ZERO;

        List<String> reasons =
                new ArrayList<>();

        /*
         * 当前经营现金流为负。
         */
        if (ocf.compareTo(BigDecimal.ZERO) < 0) {

            score =
                    score.add(
                            new BigDecimal("35")
                    );

            reasons.add(
                    "当前经营活动现金流净额为负"
            );
        }

        /*
         * 连续经营现金流为负。
         */
        int negativeCount =
                consecutiveNegativeCashFlow(rows);

        if (negativeCount >= 2) {

            score =
                    score.add(
                            new BigDecimal("30")
                    );

            reasons.add(
                    "连续"
                            + negativeCount
                            + "个报告期经营现金流为负"
            );
        }

        /*
         * 经营现金流与净利润严重背离。
         *
         * 这里不要求净利润必须存在。
         */
        FinancialRow incomeLatest =
                latestSameDate(
                        data.getIncome(),
                        latest.getReportDate()
                );

        if (incomeLatest != null) {

            BigDecimal netProfit =
                    first(
                            incomeLatest,
                            "PARENT_NETPROFIT",
                            "NETPROFIT"
                    );

            if (netProfit != null
                    && netProfit.compareTo(BigDecimal.ZERO) > 0
                    && ocf.compareTo(BigDecimal.ZERO) < 0) {

                score =
                        score.add(
                                new BigDecimal("25")
                        );

                reasons.add(
                        "净利润为正但经营现金流为负"
                );
            }
        }

        /*
         * 现金余额异常。
         */
        BigDecimal endCash =
                latest.get("END_CCE");

        if (endCash != null
                && endCash.compareTo(BigDecimal.ZERO) < 0) {

            score =
                    score.add(
                            new BigDecimal("20")
                    );

            reasons.add(
                    "期末现金及现金等价物余额为负"
            );
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }

        return available(
                RiskDimension.CASH_FLOW,
                score,
                weight,
                String.join("；", reasons)
        );
    }

    private int consecutiveNegativeCashFlow(
            List<FinancialRow> rows
    ) {

        int count = 0;

        for (int i = rows.size() - 1;
             i >= 0;
             i--) {

            BigDecimal value =
                    rows.get(i)
                            .get("NETCASH_OPERATE");

            if (value == null) {
                continue;
            }

            if (value.compareTo(BigDecimal.ZERO) < 0) {

                count++;

            } else {

                break;
            }

            if (count >= 4) {
                break;
            }
        }

        return count;
    }

    /**
     * ============================================================
     * 偿债能力
     * ============================================================
     */

    private DimensionResult evaluateSolvency(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.25");

        if (data.getBalance().isEmpty()) {

            return unavailable(
                    RiskDimension.SOLVENCY,
                    weight,
                    "资产负债表无数据"
            );
        }

        FinancialRow latest =
                data.getBalance().stream()
                        .max(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .orElse(null);

        if (latest == null) {

            return unavailable(
                    RiskDimension.SOLVENCY,
                    weight,
                    "资产负债表无有效报告期"
            );
        }

        BigDecimal assets =
                latest.get("TOTAL_ASSETS");

        BigDecimal liabilities =
                latest.get("TOTAL_LIABILITIES");

        BigDecimal currentLiab =
                latest.get("TOTAL_CURRENT_LIAB");

        BigDecimal currentAssets =
                latest.get("TOTAL_CURRENT_ASSETS");

        BigDecimal cash =
                latest.get("MONETARYFUNDS");

        if (assets == null
                && liabilities == null
                && currentAssets == null
                && currentLiab == null) {

            return unavailable(
                    RiskDimension.SOLVENCY,
                    weight,
                    "资产负债表核心字段均缺失"
            );
        }

        BigDecimal score =
                BigDecimal.ZERO;

        List<String> reasons =
                new ArrayList<>();

        /*
         * 资产负债率。
         */
        if (assets != null
                && liabilities != null
                && assets.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal debtRatio =
                    liabilities.divide(
                            assets,
                            6,
                            RoundingMode.HALF_UP
                    );

            /*
             * > 70%
             */
            if (debtRatio.compareTo(
                    new BigDecimal("0.70")
            ) > 0) {

                score =
                        score.add(
                                new BigDecimal("35")
                        );

                reasons.add(
                        "资产负债率超过70%"
                );
            }

            /*
             * > 85%
             */
            if (debtRatio.compareTo(
                    new BigDecimal("0.85")
            ) > 0) {

                score =
                        score.add(
                                new BigDecimal("25")
                        );

                reasons.add(
                        "资产负债率超过85%"
                );
            }
        }

        /*
         * 流动比率。
         */
        if (currentAssets != null
                && currentLiab != null
                && currentLiab.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal currentRatio =
                    currentAssets.divide(
                            currentLiab,
                            6,
                            RoundingMode.HALF_UP
                    );

            /*
             * < 1
             */
            if (currentRatio.compareTo(
                    BigDecimal.ONE
            ) < 0) {

                score =
                        score.add(
                                new BigDecimal("30")
                        );

                reasons.add(
                        "流动比率低于1"
                );
            }
        }

        /*
         * 货币资金明显低于流动负债。
         */
        if (cash != null
                && currentLiab != null
                && currentLiab.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal cashRatio =
                    cash.divide(
                            currentLiab,
                            6,
                            RoundingMode.HALF_UP
                    );

            if (cashRatio.compareTo(
                    new BigDecimal("0.10")
            ) < 0) {

                score =
                        score.add(
                                new BigDecimal("20")
                        );

                reasons.add(
                        "货币资金/流动负债低于10%"
                );
            }
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }

        return available(
                RiskDimension.SOLVENCY,
                score,
                weight,
                String.join("；", reasons)
        );
    }

    /**
     * ============================================================
     * 资产质量
     * ============================================================
     */

    private DimensionResult evaluateAssetQuality(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.15");

        if (data.getBalance().isEmpty()) {

            return unavailable(
                    RiskDimension.ASSET_QUALITY,
                    weight,
                    "资产负债表无数据"
            );
        }

        FinancialRow latest =
                data.getBalance().stream()
                        .max(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .orElse(null);

        if (latest == null) {

            return unavailable(
                    RiskDimension.ASSET_QUALITY,
                    weight,
                    "资产负债表无有效数据"
            );
        }

        BigDecimal assets =
                latest.get("TOTAL_ASSETS");

        BigDecimal receivable =
                latest.get("ACCOUNTS_RECE");

        BigDecimal inventory =
                latest.get("INVENTORY");

        BigDecimal currentAssets =
                latest.get(
                        "TOTAL_CURRENT_ASSETS"
                );

        if (assets == null
                && receivable == null
                && inventory == null) {

            return unavailable(
                    RiskDimension.ASSET_QUALITY,
                    weight,
                    "资产质量关键字段缺失"
            );
        }

        BigDecimal score =
                BigDecimal.ZERO;

        List<String> reasons =
                new ArrayList<>();

        /*
         * 应收账款 / 总资产。
         */
        if (receivable != null
                && assets != null
                && assets.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal ratio =
                    receivable.divide(
                            assets,
                            6,
                            RoundingMode.HALF_UP
                    );

            if (ratio.compareTo(
                    new BigDecimal("0.30")
            ) > 0) {

                score =
                        score.add(
                                new BigDecimal("25")
                        );

                reasons.add(
                        "应收账款占总资产超过30%"
                );
            }

            if (ratio.compareTo(
                    new BigDecimal("0.50")
            ) > 0) {

                score =
                        score.add(
                                new BigDecimal("25")
                        );

                reasons.add(
                        "应收账款占总资产超过50%"
                );
            }
        }

        /*
         * 存货 / 流动资产。
         */
        if (inventory != null
                && currentAssets != null
                && currentAssets.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal ratio =
                    inventory.divide(
                            currentAssets,
                            6,
                            RoundingMode.HALF_UP
                    );

            if (ratio.compareTo(
                    new BigDecimal("0.50")
            ) > 0) {

                score =
                        score.add(
                                new BigDecimal("25")
                        );

                reasons.add(
                        "存货占流动资产超过50%"
                );
            }
        }

        /*
         * 应收账款增长。
         */
        FinancialRow previous =
                previousSamePeriod(
                        latest,
                        data.getBalance()
                );

        if (previous != null) {

            BigDecimal previousReceivable =
                    previous.get("ACCOUNTS_RECE");

            if (receivable != null
                    && previousReceivable != null
                    && previousReceivable.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal growth =
                        receivable
                                .subtract(previousReceivable)
                                .divide(
                                        previousReceivable,
                                        6,
                                        RoundingMode.HALF_UP
                                );

                if (growth.compareTo(
                        new BigDecimal("0.50")
                ) > 0) {

                    score =
                            score.add(
                                    new BigDecimal("25")
                            );

                    reasons.add(
                            "应收账款同比增长超过50%"
                    );
                }
            }
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }

        return available(
                RiskDimension.ASSET_QUALITY,
                score,
                weight,
                String.join("；", reasons)
        );
    }

    /**
     * ============================================================
     * 利润质量
     * ============================================================
     */

    private DimensionResult evaluateProfitQuality(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.10");

        if (data.getIncome().isEmpty()
                || data.getCashFlow().isEmpty()) {

            return unavailable(
                    RiskDimension.PROFIT_QUALITY,
                    weight,
                    "利润表或现金流量表缺失"
            );
        }

        FinancialRow latestIncome =
                data.getIncome().stream()
                        .max(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .orElse(null);

        FinancialRow latestCash =
                data.getCashFlow().stream()
                        .max(
                                Comparator.comparing(
                                        r -> r.getReportDate()
                                )
                        )
                        .orElse(null);

        if (latestIncome == null
                || latestCash == null) {

            return unavailable(
                    RiskDimension.PROFIT_QUALITY,
                    weight,
                    "利润质量数据不足"
            );
        }

        BigDecimal netProfit =
                first(
                        latestIncome,
                        "PARENT_NETPROFIT",
                        "NETPROFIT"
                );

        BigDecimal deductProfit =
                latestIncome.get(
                        "DEDUCT_PARENT_NETPROFIT"
                );

        BigDecimal operatingCash =
                latestCash.get(
                        "NETCASH_OPERATE"
                );

        if (netProfit == null
                && deductProfit == null
                && operatingCash == null) {

            return unavailable(
                    RiskDimension.PROFIT_QUALITY,
                    weight,
                    "利润质量字段缺失"
            );
        }

        BigDecimal score =
                BigDecimal.ZERO;

        List<String> reasons =
                new ArrayList<>();

        /*
         * 扣非净利润为负。
         */
        if (deductProfit != null
                && deductProfit.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            score =
                    score.add(
                            new BigDecimal("35")
                    );

            reasons.add(
                    "扣非归母净利润为负"
            );
        }

        /*
         * 净利润为正但扣非为负。
         */
        if (netProfit != null
                && deductProfit != null
                && netProfit.compareTo(BigDecimal.ZERO) > 0
                && deductProfit.compareTo(BigDecimal.ZERO) < 0) {

            score =
                    score.add(
                            new BigDecimal("25")
                    );

            reasons.add(
                    "净利润为正但扣非净利润为负"
            );
        }

        /*
         * 净利润为正、经营现金流为负。
         */
        if (netProfit != null
                && operatingCash != null
                && netProfit.compareTo(BigDecimal.ZERO) > 0
                && operatingCash.compareTo(BigDecimal.ZERO) < 0) {

            score =
                    score.add(
                            new BigDecimal("40")
                    );

            reasons.add(
                    "净利润为正但经营现金流为负"
            );
        }

        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }

        return available(
                RiskDimension.PROFIT_QUALITY,
                score,
                weight,
                String.join("；", reasons)
        );
    }

    /**
     * ============================================================
     * 风险评分
     * ============================================================
     */

    private BigDecimal calculateRiskScore(
            List<DimensionResult> dimensions
    ) {

        BigDecimal numerator =
                BigDecimal.ZERO;

        BigDecimal denominator =
                BigDecimal.ZERO;

        for (DimensionResult dimension :
                dimensions) {

            if (!dimension.isAvailable()) {
                continue;
            }

            numerator =
                    numerator.add(
                            dimension.getScore()
                                    .multiply(
                                            dimension.getWeight()
                                    )
                    );

            denominator =
                    denominator.add(
                            dimension.getWeight()
                    );
        }

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {

            return BigDecimal.ZERO;
        }

        return numerator.divide(
                denominator,
                4,
                RoundingMode.HALF_UP
        );
    }

    private RiskLevel resolveRiskLevel(
            BigDecimal score,
            BigDecimal availableWeight
    ) {

        if (availableWeight.compareTo(
                MIN_AVAILABLE_WEIGHT
        ) < 0) {

            return RiskLevel.UNKNOWN;
        }

        if (score.compareTo(
                new BigDecimal("80")
        ) >= 0) {

            return RiskLevel.HIGH;
        }

        if (score.compareTo(
                new BigDecimal("60")
        ) >= 0) {

            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    /**
     * ============================================================
     * 技术选股资格
     * ============================================================
     */

    private boolean resolveTechnicalSelectionEligibility(
            FinancialRiskResult result,
            DataQuality quality
    ) {

        /*
         * 真正高风险：
         * 直接剔除。
         */
        if (result.getRiskLevel() == RiskLevel.HIGH) {
            return false;
        }

        /*
         * 数据完全不足：
         * 无法进行可靠财务过滤。
         */
        if (quality.getDataLevel() == DataLevel.INSUFFICIENT) {

            return false;
        }

        /*
         * 没有任何利润表和资产负债表：
         * 无法判断企业基本面。
         */
        if (quality.getIncomePeriods() == 0
                && quality.getBalancePeriods() == 0) {

            return false;
        }

        /*
         * 数据存在严重结构性异常：
         *
         * 例如：
         *
         * income = 0
         * balance = 5
         * cash = 12
         *
         * 这种情况可以做有限分析，
         * 但不建议直接进入自动化技术选股。
         */
        if (quality.getDataLevel() == DataLevel.DATA_ANOMALY) {

            /*
             * 如果至少有利润表或资产负债表 6 个以上，
             * 允许继续，但结果必须保留 DATA_ANOMALY。
             */
            int usable =
                    Math.max(
                            quality.getIncomePeriods(),
                            quality.getBalancePeriods()
                    );

            return usable >= 6;
        }

        /*
         * 历史较短：
         *
         * 新股、北交所新上市股票等，
         * 不因为历史不足直接判财务风险。
         *
         * 只要有至少4个报告期，
         * 且不是高风险，可以继续。
         */
        if (quality.getDataLevel() == DataLevel.LIMITED_HISTORY) {

            return result.getObservedPeriods()
                    >= LIMITED_HISTORY_PERIODS;
        }

        /*
         * DATA_GAP：
         * 数据不完整，但可以继续。
         */
        return true;
    }

    /**
     * ============================================================
     * 同比 / 报告期处理
     * ============================================================
     */

    /**
     * 找同比报告期。
     *
     * 核心：
     *
     * 不能简单：
     *
     * reportDate.minusYears(1)
     *
     * 然后无脑比较。
     *
     * 必须保证：
     *
     * Q1 -> Q1
     * H1 -> H1
     * Q3 -> Q3
     * FY -> FY
     */
    private FinancialRow findYoYPeriod(
            FinancialRow current,
            List<FinancialRow> rows
    ) {

        if (current == null
                || current.getReportDate() == null) {

            return null;
        }

        ReportPeriodType currentType =
                resolveReportPeriodType(current);

        if (currentType
                == ReportPeriodType.UNKNOWN) {

            return null;
        }

        LocalDate targetDate =
                current.getReportDate().minusYears(1);

        for (FinancialRow row : rows) {

            if (row.getReportDate() == null) {
                continue;
            }

            if (!targetDate.equals(
                    row.getReportDate()
            )) {
                continue;
            }

            ReportPeriodType rowType =
                    resolveReportPeriodType(row);

            if (rowType == currentType) {
                return row;
            }
        }

        /*
         * 如果 REPORT_DATE_NAME / REPORT_TYPE 不标准，
         * 再进行严格日期匹配。
         */
        return rows.stream()
                .filter(
                        r -> targetDate.equals(
                                r.getReportDate()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * 找上一年相同报告期。
     */
    private FinancialRow previousSamePeriod(
            FinancialRow current,
            List<FinancialRow> rows
    ) {

        return findYoYPeriod(
                current,
                rows
        );
    }

    private ReportPeriodType resolveReportPeriodType(
            FinancialRow row
    ) {

        String name =
                row.getReportDateName() == null
                        ? ""
                        : row.getReportDateName();

        String type =
                row.getReportType() == null
                        ? ""
                        : row.getReportType();

        String text =
                name + " " + type;

        if (text.contains("一季报")
                || text.contains("一季")
                || text.contains("Q1")) {

            return ReportPeriodType.Q1;
        }

        if (text.contains("中报")
                || text.contains("半年报")
                || text.contains("H1")
                || text.contains("半年")) {

            return ReportPeriodType.H1;
        }

        if (text.contains("三季报")
                || text.contains("Q3")
                || text.contains("三季")) {

            return ReportPeriodType.Q3;
        }

        if (text.contains("年报")
                || text.contains("年度")
                || text.contains("FY")) {

            return ReportPeriodType.FY;
        }

        /*
         * 根据日期兜底。
         */
        if (row.getReportDate() != null) {

            int month =
                    row.getReportDate().getMonthValue();

            int day =
                    row.getReportDate().getDayOfMonth();

            if (month == 3
                    && day == 31) {

                return ReportPeriodType.Q1;
            }

            if (month == 6
                    && day == 30) {

                return ReportPeriodType.H1;
            }

            if (month == 9
                    && day == 30) {

                return ReportPeriodType.Q3;
            }

            if (month == 12
                    && day == 31) {

                return ReportPeriodType.FY;
            }
        }

        return ReportPeriodType.UNKNOWN;
    }

    /**
     * ============================================================
     * 辅助方法
     * ============================================================
     */

    private FinancialRow latestSameDate(
            List<FinancialRow> rows,
            LocalDate date
    ) {

        if (date == null) {
            return null;
        }

        return rows.stream()
                .filter(
                        r -> date.equals(
                                r.getReportDate()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private BigDecimal first(
            FinancialRow row,
            String... columns
    ) {

        if (row == null) {
            return null;
        }

        for (String column : columns) {

            BigDecimal value =
                    row.get(column);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private DimensionResult available(
            RiskDimension dimension,
            BigDecimal score,
            BigDecimal weight,
            String reason
    ) {

        return new DimensionResult(
                dimension,
                true,
                score,
                weight,
                reason
        );
    }

    private DimensionResult unavailable(
            RiskDimension dimension,
            BigDecimal weight,
            String reason
    ) {

        return new DimensionResult(
                dimension,
                false,
                BigDecimal.ZERO,
                weight,
                reason
        );
    }

    private BigDecimal scale(
            BigDecimal value
    ) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}