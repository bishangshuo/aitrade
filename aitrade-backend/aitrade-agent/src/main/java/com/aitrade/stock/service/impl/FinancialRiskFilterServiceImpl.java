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


    /**
     * 构造方法，注入 TimescaleDB/PostgreSQL 的 JdbcTemplate。
     *
     * @param jdbcTemplate 用于直接查询三张财务表的 JDBC 模板
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

        // 入参基本校验：证券代码与截止日期均不允许为空
        if (!StringUtils.hasText(secuCode)) {
            throw new IllegalArgumentException("secuCode不能为空");
        }

        if (asOfDate == null) {
            throw new IllegalArgumentException("asOfDate不能为空");
        }

        // 统一标准化：去空格并转大写，保证与库内 SECUCODE 写法一致
        secuCode = secuCode.trim().toUpperCase();

        // 第一步：加载截至 asOfDate 已可见的三张表数据
        FinancialData data =
                loadFinancialData(secuCode, asOfDate);

        // 第二步：先评估数据质量（缺哪些期、覆盖率、质量等级）
        DataQuality quality = analyzeDataQuality(data);

        FinancialRiskResult result = new FinancialRiskResult();

        // 以下将本次分析上下文与质量统计结果回填到输出对象
        result.setAsOfDate(asOfDate);

        result.setSecuCode(secuCode);

        result.setDataLevel(quality.getDataLevel());

        result.setExpectedPeriods(quality.getExpectedPeriods());

        result.setIncomePeriods(quality.getIncomePeriods());

        result.setBalancePeriods(quality.getBalancePeriods());

        result.setCashFlowPeriods(quality.getCashFlowPeriods());

        result.setCompletePeriods(quality.getCompletePeriods());

        // 观测期数：取三张表中覆盖最多的一张，代表实际可用历史长度
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
         *
         * 三表均无任何报告期：不判风险也不判选股，
         * 直接返回 UNKNOWN，避免把“没数据”误当“有风险”。
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
         *
         * 五个维度依次评估，每个维度内部自行判断数据是否可用。
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
         *
         * 仅可计算（available）的维度才写入得分与风险理由，
         * 缺失维度不参与，不强行给分也不记 0。
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
         *
         * availableWeight：所有可计算维度的权重之和，
         * 用于衡量本次评分到底基于了多少可用信息。
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

    /**
     * 一次性加载指定股票、截至 asOfDate 已可见的全部财务数据。
     *
     * 三张表分别独立加载（利润表、资产负债表、现金流量表），
     * 加载完成后再各自构建“报告期 -> 行数据”的索引 Map，
     * 供后续数据质量分析与各风险维度评估复用。
     *
     * @param secuCode 证券代码（含市场后缀，已大写）
     * @param asOfDate 历史可见性截止日，只加载该日期之前已披露的报告
     * @return 聚合后的财务数据容器
     */
    private FinancialData loadFinancialData(
            String secuCode,
            LocalDate asOfDate
    ) {

        // 三张表各自独立加载，互不作为前提（不用 INNER JOIN）
        FinancialData data = new FinancialData();

        data.setIncome(loadIncomeStatement(secuCode, asOfDate));

        data.setBalance(loadBalanceSheet(secuCode, asOfDate));

        data.setCashFlow(loadCashFlowStatement(secuCode, asOfDate));

        // 各自按“报告期 -> 行数据”建索引，供后续快速查找同期数据
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
                // 外层：只选取利润表所需字段
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
                // 内层：同一报告期可能存在多个披露版本（如更正公告），
                // 用 ROW_NUMBER 按 披露日期/更新日期 降序编号，
                // 外层仅保留 rn = 1，即截至查询时点的最新版本文档
                "FROM ( " +
                    "SELECT *, " +
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

        // 截止参数取 asOfDate 次日零点，用 < 比较等价于“含当日全部披露”
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
                // 外层：只选取资产负债表所需字段
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
                // 内层：同上，按报告期取截至时点的最新披露版本
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

        // 截止参数取 asOfDate 次日零点，用 < 比较等价于“含当日全部披露”
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
                // 外层：只选取现金流量表所需字段
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
                // 内层：同上，按报告期取截至时点的最新披露版本
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

        // 截止参数取 asOfDate 次日零点，用 < 比较等价于“含当日全部披露”
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

    /**
     * 利润表结果集行映射器。
     *
     * 先构建包含公共元信息（报告期、披露日期等）的基础行，
     * 再逐字段读取盈利相关指标写入行容器；字段为 NULL 时不写入，
     * 以便下游区分“缺失”与“数值为 0”。
     *
     * 注：以下 put() 均为“列名 -> 行内键名”的一一映射，
     * 键名与数据库列名保持一致，取值时缺失字段不写入。
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

    /**
     * 资产负债表结果集行映射器。
     *
     * 读取货币资金、流动/非流动资产与负债、总资产、总负债、
     * 股东权益、应收账款、存货等偿债与资产质量相关字段。
     *
     * 注：TOTAL_NONCURRENT_ASSETS / TOTAL_NONCURRENT_LIAB /
     * TOTAL_LIAB_EQUITY 当前已读取但评分逻辑未直接使用。
     */
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

    /**
     * 现金流量表结果集行映射器。
     *
     * 读取经营/投资/筹资活动现金流净额、现金及现金等价物净增加额、
     * 期初期末现金余额等现金流相关字段。
     */
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

    /**
     * 构建基础行：读取三张表共有的元信息字段。
     *
     * 包括证券代码、报告期（REPORT_DATE）、报告类型/名称、
     * 公告日期（NOTICE_DATE）、更新日期（UPDATE_DATE）。
     * 时间戳统一按系统时区转换为 LocalDate，为空则保持 null。
     */
    private FinancialRow baseRow(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        FinancialRow row = new FinancialRow();

        row.setSecuCode(rs.getString("SECUCODE"));

        // 报告期：时间戳非空才按系统时区取日期，为空则保留 null
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

        // 公告日期（NOTICE_DATE）：历史可见性截断的基准时间
        Timestamp noticeTimestamp =
                rs.getTimestamp("NOTICE_DATE");

        if (noticeTimestamp != null) {

            row.setNoticeDate(noticeTimestamp
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        }

        // 更新日期（UPDATE_DATE）：同一披露时点内的版本排序辅助字段
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

    /**
     * 安全地把结果集中的数值字段写入行容器。
     *
     * 关键点：当数据库字段为 NULL 时不写入，避免把“数据缺失”
     * 误当作 0 参与指标计算，符合“数据缺失 != 财务风险”原则。
     *
     * @param row    目标行
     * @param key    行内存放该指标的键名
     * @param rs     结果集
     * @param column 数据库列名
     */
    private void put(
            FinancialRow row,
            String key,
            java.sql.ResultSet rs,
            String column
    ) throws java.sql.SQLException {

        BigDecimal value = rs.getBigDecimal(column);

        // 仅在非 NULL 时写入：NULL 代表数据缺失，不能当作 0
        if (value != null) {
            row.getValues().put(key, value);
        }
    }

    /**
     * 将行列表按报告期构建索引 Map。
     *
     * 过滤掉报告期缺失的行；同一报告期若出现重复，保留先出现的行，
     * 便于后续按日期快速定位某张表的某个报告期数据。
     */
    private Map<LocalDate, FinancialRow> toMap(
            List<FinancialRow> rows
    ) {

        return rows.stream()
                .filter(r -> r.getReportDate() != null)
                .collect(
                        Collectors.toMap(
                                r -> r.getReportDate(),
                                r -> r,
                                // 合并函数：同报告期重复时保留先入者 (a)
                                (a, b) -> a
                        )
                );
    }

    /**
     * ============================================================
     * 数据质量
     * ============================================================
     */

    /**
     * 分析三张表的数据质量。
     *
     * 以最近的 12 个真实报告期（{@link #EXPECTED_PERIOD_COUNT}）为基准，
     * 分别统计利润表/资产负债表/现金流量表的覆盖期数与缺失报告期，
     * 计算各表覆盖率，并据此判定整体数据质量等级（DataLevel）：
     * <ul>
     *   <li>INSUFFICIENT：有效历史严重不足，基本无法分析</li>
     *   <li>LIMITED_HISTORY：历史较短，长期趋势可信度有限</li>
     *   <li>FULL：三张表在目标期内完全齐备</li>
     *   <li>DATA_ANOMALY：至少一张核心表完全缺失（结构性缺失，非财务风险）</li>
     *   <li>DATA_GAP：三张表存在部分报告期缺失</li>
     * </ul>
     * 同时将发现的缺失情况记录为告警信息，供结果输出展示。
     *
     * @param data 已加载的三张表财务数据
     * @return 数据质量分析结果
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

        // 取三张表实际存在的报告期集合，用于比对期望期是否缺失
        Set<LocalDate> incomeDates =
                data.getIncomeMap().keySet();

        Set<LocalDate> balanceDates =
                data.getBalanceMap().keySet();

        Set<LocalDate> cashDates =
                data.getCashFlowMap().keySet();

        // 逐个检查期望报告期，分别记录三张表各自的缺失期
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

        // 各表有效期数 = 期望期数 - 该表缺失期数
        quality.setIncomePeriods(quality.getExpectedPeriods()
                        - quality.getMissingIncomePeriods().size());

        quality.setBalancePeriods(quality.getExpectedPeriods()
                        - quality.getMissingBalancePeriods().size());

        quality.setCashFlowPeriods(quality.getExpectedPeriods()
                        - quality.getMissingCashFlowPeriods().size());

        quality.setCompletePeriods(quality.getExpectedPeriods()
                        - quality.getMissingCompletePeriods().size());

        // 将各表有效期数换算为 0~1 的覆盖率，供结果展示
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
         *
         * maxPeriods：三表中覆盖最多的一张，代表可用历史上限；
         * minPeriods：三表中覆盖最少的一张，用于识别结构性缺失。
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

        // 优先级从上到下：先判量不足，再判历史短，再判完全齐备，
        // 再判结构性缺失，最后兼作部分缺失（DATA_GAP）
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

        // 三表完全齐备：期望期与实际完整覆盖期一一对应
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
                // 三张表的报告期 UNION 合并后去重（UNION 自身去重，
                // DISTINCT 再保险），截至今日的不多于 latestDate 的报告期中
                // 按时间倒序取最近 12 个，作为“期望报告期”基准
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

    /**
     * 取三张表中最大的报告期作为当前财务数据终点。
     *
     * @param data 已加载的财务数据
     * @return 最晚报告期，无任何数据时返回 null
     */
    private LocalDate latestReportDate(
            FinancialData data
    ) {

        return StreamMax(
                data.getIncome(),
                data.getBalance(),
                data.getCashFlow()
        );
    }

    /**
     * 合并三张表的行数据，计算其中的最大报告期。
     *
     * 忽略报告期缺失的行；全部为空时返回 null。
     * （注：方法名首字母大写为现有命名，未作修改。）
     */
    private LocalDate StreamMax(
            List<FinancialRow> income,
            List<FinancialRow> balance,
            List<FinancialRow> cashFlow
    ) {

        // 三张表的行合并成一条流，取报告期非空的最大值
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

    /**
     * 计算覆盖率 = 实际有数据期数 / 期望期数。
     *
     * 保留 4 位小数；期望期数 <= 0 时直接返回 0，避免除零。
     */
    private BigDecimal coverage(
            int actual,
            int expected
    ) {

        // 期望期数无效（<=0）时直接返回 0，避免除零
        if (expected <= 0) {
            return BigDecimal.ZERO;
        }

        // 实际期数 / 期望期数，保留 4 位小数
        return BigDecimal
                .valueOf(actual)
                .divide(
                        BigDecimal.valueOf(expected),
                        4,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * 汇总三张表缺失报告期的并集。
     *
     * 使用 TreeSet 去重并按日期升序，便于在结果中统一展示缺失区间。
     */
    private List<LocalDate> unionMissingPeriods(
            DataQuality quality
    ) {

        // TreeSet 自动去重并按日期升序排列
        Set<LocalDate> result =
                new TreeSet<>();

        // 依次并入三张表各自的缺失报告期
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

    /**
     * 盈利能力维度评估（权重 0.25）。
     *
     * 基于利润表按报告期升序排列后的最新一行，逐项判定：
     * <ul>
     *   <li>当前净利润为负：+40</li>
     *   <li>营业收入非正：+30</li>
     *   <li>连续 ≥ 2 个报告期亏损：+20</li>
     *   <li>归母净利润同比由正转负（仅用相同报告期类型）：+20</li>
     * </ul>
     * 得分上限 100。利润表无数据或关键盈利字段缺失时该维度不可用。
     * 净利润优先取归母净利润（PARENT_NETPROFIT），回退到 NETPROFIT。
     */
    private DimensionResult evaluateProfitability(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.25");

        // 利润表行按报告期升序排列，便于取末行为最新期
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

        // 升序排列后末行即最新报告期
        FinancialRow latest =
                rows.get(rows.size() - 1);

        // 净利润优先取归母净利润，缺失时回退到净利润
        BigDecimal netProfit =
                first(
                        latest,
                        "PARENT_NETPROFIT",
                        "NETPROFIT"
                );

        // 营收优先取营业总收入，缺失时回退到营业收入
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

        // 得分上限截断到 100，避免多项叠加超过百分制
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

    /**
     * 从最新报告期往前回溯，统计归母净利润连续为负的报告期数。
     *
     * 只统计实际存在的数据，字段缺失则跳过（不计入也不中断），
     * 遇到第一个盈利（≥ 0）即中断，最多统计 4 个报告期。
     */
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

            // 字段缺失（为 null）：不计入也不中断，跳过继续往前看
            if (profit == null) {
                continue;
            }

            // 为负则连续计数，遇到首个非负值即中断回溯
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

    /**
     * 经营现金流维度评估（权重 0.25）。
     *
     * 基于现金流量表最新一行，逐项判定：
     * <ul>
     *   <li>当前经营现金流净额为负：+35</li>
     *   <li>连续 ≥ 2 个报告期经营现金流为负：+30</li>
     *   <li>净利润为正但经营现金流为负（背离）：+25</li>
     *   <li>期末现金及现金等价物余额为负：+20</li>
     * </ul>
     * 得分上限 100。无现金流表或经营现金流净额缺失时该维度不可用。
     */
    private DimensionResult evaluateCashFlow(
            FinancialData data
    ) {

        BigDecimal weight =
                new BigDecimal("0.25");

        // 现金流量表行按报告期升序，末行为最新期
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

        // 取最新报告期的经营活动现金流净额（OCF），缺失则本维度不可用
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
        // 跨表对齐：取与现金流最新期同一报告期的利润表行
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

    /**
     * 从最新报告期往前回溯，统计经营现金流净额连续为负的报告期数。
     *
     * 逻辑与 {@link #consecutiveNegativeNetProfit} 一致：
     * 缺失字段跳过，遇到非负值中断，最多统计 4 个报告期。
     */
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

            // 为负则连续计数，遇到首个非负值即中断回溯
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

    /**
     * 偿债能力维度评估（权重 0.25）。
     *
     * 基于资产负债表最新一行，逐项判定：
     * <ul>
     *   <li>资产负债率 > 70%：+35；> 85%：额外再 +25</li>
     *   <li>流动比率（流动资产/流动负债） < 1：+30</li>
     *   <li>货币资金/流动负债 < 10%：+20</li>
     * </ul>
     * 得分上限 100。无资产负债表或核心字段均缺失时该维度不可用。
     * 所有比率仅在分母 > 0 时才计算，避免除零。
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

        // 取资产负债表中的最新报告期行
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

            // 资产负债率 = 总负债 / 总资产，保留 6 位小数
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

        // 得分上限截断到 100，避免多项叠加超过百分制
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

    /**
     * 资产质量维度评估（权重 0.15）。
     *
     * 基于资产负债表最新一行，逐项判定：
     * <ul>
     *   <li>应收账款/总资产 > 30%：+25；> 50%：额外再 +25</li>
     *   <li>存货/流动资产 > 50%：+25</li>
     *   <li>应收账款同比增长 > 50%：+25</li>
     * </ul>
     * 得分上限 100。无资产负债表或关键字段缺失时该维度不可用。
     * 同比仅在上一年相同报告期存在时才计算。
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

        // 取资产负债表中的最新报告期行
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

        // 取出资产质量相关关键字段（总资产/应收账款/存货/流动资产）
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

            // 应收账款占总资产比例，保留 6 位小数
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

            // 存货占流动资产比例，保留 6 位小数
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

                // 应收账款同比增长率 = (本期 - 上年同期) / 上年同期
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

        // 得分上限截断到 100，避免多项叠加超过百分制
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

    /**
     * 利润质量维度评估（权重 0.10）。
     *
     * 需同时使用利润表与现金流量表最新行，逐项判定：
     * <ul>
     *   <li>扣非归母净利润为负：+35</li>
     *   <li>净利润为正但扣非净利润为负：+25</li>
     *   <li>净利润为正但经营现金流为负：+40</li>
     * </ul>
     * 得分上限 100。利润表或现金流量表缺失、相关字段缺失时不可用。
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

        // 分别取利润表与现金流量表的最新报告期行
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

        // 取利润质量相关字段：净利润（回退）、扣非归母、经营现金流
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

        // 得分上限截断到 100，避免多项叠加超过百分制
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

    /**
     * 计算按实际可计算维度动态归一化的风险总分。
     *
     * 采用加权平均：总分 = Σ(维度得分 × 维度权重) / Σ(可用维度权重)。
     * 仅统计 available=true 的维度，缺失数据的维度不参与计分也不分摊权重，
     * 从而避免因数据不全而拉低或抬高总分，符合“按实际可计算维度动态归一化”。
     * 无任何可用维度（权重和为 0）时返回 0。
     */
    private BigDecimal calculateRiskScore(
            List<DimensionResult> dimensions
    ) {

        // numerator = Σ(得分×权重)；denominator = Σ(可用维度权重)
        BigDecimal numerator =
                BigDecimal.ZERO;

        BigDecimal denominator =
                BigDecimal.ZERO;

        for (DimensionResult dimension :
                dimensions) {

            // 不可计算维度直接跳过，既不参分也不分摊权重
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

        // 无任何可用维度（权重和为 0）时避免除零，返回 0
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {

            return BigDecimal.ZERO;
        }

        return numerator.divide(
                denominator,
                4,
                RoundingMode.HALF_UP
        );
    }

    /**
     * 根据风险总分与可计算权重映射风险等级。
     *
     * 可计算权重不足 MIN_AVAILABLE_WEIGHT 时判为 UNKNOWN（不强行判风险）；
     * 否则：得分 ≥ 80 为 HIGH，≥ 60 为 MEDIUM，其余为 LOW。
     */
    private RiskLevel resolveRiskLevel(
            BigDecimal score,
            BigDecimal availableWeight
    ) {

        // 可计算权重不足：信息太少，不强行定级，返回 UNKNOWN
        if (availableWeight.compareTo(
                MIN_AVAILABLE_WEIGHT
        ) < 0) {

            return RiskLevel.UNKNOWN;
        }

        // 得分 >= 80：高风险
        if (score.compareTo(
                new BigDecimal("80")
        ) >= 0) {

            return RiskLevel.HIGH;
        }

        // 得分 >= 60：中等风险
        if (score.compareTo(
                new BigDecimal("60")
        ) >= 0) {

            return RiskLevel.MEDIUM;
        }

        // 其余：低风险
        return RiskLevel.LOW;
    }

    /**
     * ============================================================
     * 技术选股资格
     * ============================================================
     */

    /**
     * 判定是否允许进入后续技术面选股。
     *
     * 与“是否存在财务风险”相互独立，侧重于数据充分性：
     * <ul>
     *   <li>HIGH 风险：直接剔除</li>
     *   <li>INSUFFICIENT（数据完全不足）：无法可靠过滤，剔除</li>
     *   <li>无任何利润表且无资产负债表：无法判断基本面，剔除</li>
     *   <li>DATA_ANOMALY（结构性缺失）：仅在利润表或资产负债表 ≥ 6 期时才允许</li>
     *   <li>LIMITED_HISTORY（历史较短）：只要实际观测期数 ≥ 4 即允许</li>
     *   <li>DATA_GAP：数据不完整但可继续，允许</li>
     * </ul>
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

        // 先解析当前行报告期类型；类型未知则无法做同类型同比
        ReportPeriodType currentType =
                resolveReportPeriodType(current);

        if (currentType
                == ReportPeriodType.UNKNOWN) {

            return null;
        }

        // 目标同比日：同报告期往前推一年
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

            // 优先匹配：日期相同且报告期类型相同（Q1→Q1、H1→H1 等）
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

    /**
     * 解析行数据所属的报告期类型（一季报/中报/三季报/年报）。
     *
     * 优先根据 REPORT_DATE_NAME 与 REPORT_TYPE 的文本关键字识别；
     * 都无法匹配时，再按报告期的月份+日期兜底（如 3-31、6-30、9-30、12-31）；
     * 仍无法判定时返回 UNKNOWN。
     */
    private ReportPeriodType resolveReportPeriodType(
            FinancialRow row
    ) {

        // 文本字段为 null 时用空串占位，避免拼接时出现 "null"
        String name =
                row.getReportDateName() == null
                        ? ""
                        : row.getReportDateName();

        String type =
                row.getReportType() == null
                        ? ""
                        : row.getReportType();

        // 名称 + 类型合并为一段文本，统一做关键字匹配
        String text =
                name + " " + type;

        // 关键字识别：一季报
        if (text.contains("一季报")
                || text.contains("一季")
                || text.contains("Q1")) {

            return ReportPeriodType.Q1;
        }

        // 关键字识别：中报/半年报
        if (text.contains("中报")
                || text.contains("半年报")
                || text.contains("H1")
                || text.contains("半年")) {

            return ReportPeriodType.H1;
        }

        // 关键字识别：三季报
        if (text.contains("三季报")
                || text.contains("Q3")
                || text.contains("三季")) {

            return ReportPeriodType.Q3;
        }

        // 关键字识别：年报
        if (text.contains("年报")
                || text.contains("年度")
                || text.contains("FY")) {

            return ReportPeriodType.FY;
        }

        /*
         * 根据日期兜底。
         */
        // 关键字都匹配不上时，按报告期的“月-日”推断类型
        if (row.getReportDate() != null) {

            int month =
                    row.getReportDate().getMonthValue();

            int day =
                    row.getReportDate().getDayOfMonth();

            // 3-31 -> 一季报
            if (month == 3
                    && day == 31) {

                return ReportPeriodType.Q1;
            }

            // 6-30 -> 中报
            if (month == 6
                    && day == 30) {

                return ReportPeriodType.H1;
            }

            // 9-30 -> 三季报
            if (month == 9
                    && day == 30) {

                return ReportPeriodType.Q3;
            }

            // 12-31 -> 年报
            if (month == 12
                    && day == 31) {

                return ReportPeriodType.FY;
            }
        }

        // 均无法判定
        return ReportPeriodType.UNKNOWN;
    }

    /**
     * ============================================================
     * 辅助方法
     * ============================================================
     */

    /**
     * 在指定行列表中查找与目标报告期相同的第一行。
     *
     * 用于跨表对齐（如按现金流最新报告期去利润表取同期净利润）。
     */
    private FinancialRow latestSameDate(
            List<FinancialRow> rows,
            LocalDate date
    ) {

        // 目标日期为空直接返回 null，避免无意义扫描
        if (date == null) {
            return null;
        }

        // 流式过滤出报告期等于目标日期的第一行
        return rows.stream()
                .filter(
                        r -> date.equals(
                                r.getReportDate()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * 按优先级依次取行中第一个非空的指标值。
     *
     * 用于字段回退，例如优先取归母净利润、其次取净利润。
     */
    private BigDecimal first(
            FinancialRow row,
            String... columns
    ) {

        // 行为空直接返回，防止 NPE
        if (row == null) {
            return null;
        }

        // 按传入列名顺序查找，命中第一个非空值即返回
        for (String column : columns) {

            BigDecimal value =
                    row.get(column);

            if (value != null) {
                return value;
            }
        }

        // 所有候选列均无值
        return null;
    }

    /**
     * 构造一个“可计算”的风险维度结果（available=true）。
     */
    private DimensionResult available(
            RiskDimension dimension,
            BigDecimal score,
            BigDecimal weight,
            String reason
    ) {

        // available 置 true，表示该维度基于现有数据完成了打分
        return new DimensionResult(
                dimension,
                true,
                score,
                weight,
                reason
        );
    }

    /**
     * 构造一个“不可计算”的风险维度结果（available=false）。
     *
     * 得分置 0，但仍保留权重，用于记录该维度因数据缺失而跳过。
     */
    private DimensionResult unavailable(
            RiskDimension dimension,
            BigDecimal weight,
            String reason
    ) {

        // available 置 false，得分固定为 0，reason 记录不可用原因
        return new DimensionResult(
                dimension,
                false,
                BigDecimal.ZERO,
                weight,
                reason
        );
    }

    /**
     * 将数值统一保留 2 位小数（四舍五入）；null 安全返回 0。
     */
    private BigDecimal scale(
            BigDecimal value
    ) {

        // null 视为 0，保证输出字段非空
        if (value == null) {
            return BigDecimal.ZERO;
        }

        // 统一保留 2 位小数，四舍五入
        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}