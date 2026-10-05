package com.aitrade.stock.repository;

import com.aitrade.stock.domain.StockIncomeStatement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IncomeStatementRepository {
    private static final String COLUMNS =
            "\"SECUCODE\", \"SECURITY_CODE\", \"SECURITY_NAME_ABBR\", \"ORG_CODE\", \"ORG_TYPE\", " +
            "\"REPORT_DATE\", \"REPORT_TYPE\", \"REPORT_DATE_NAME\", \"SECURITY_TYPE_CODE\", " +
            "\"NOTICE_DATE\", \"UPDATE_DATE\", \"CURRENCY\", " +
            "\"TOTAL_OPERATE_INCOME\", \"TOTAL_OPERATE_INCOME_YOY\", \"OPERATE_INCOME\", " +
            "\"OPERATE_INCOME_YOY\", \"INTEREST_INCOME\", \"FEE_COMMISSION_INCOME\", " +
            "\"OTHER_BUSINESS_INCOME\", \"TOTAL_OPERATE_COST\", \"TOTAL_OPERATE_COST_YOY\", " +
            "\"OPERATE_COST\", \"OPERATE_COST_YOY\", \"INTEREST_EXPENSE\", " +
            "\"FEE_COMMISSION_EXPENSE\", \"RESEARCH_EXPENSE\", \"RESEARCH_EXPENSE_YOY\", " +
            "\"OPERATE_TAX_ADD\", \"OPERATE_TAX_ADD_YOY\", \"SALE_EXPENSE\", \"SALE_EXPENSE_YOY\", " +
            "\"MANAGE_EXPENSE\", \"MANAGE_EXPENSE_YOY\", \"FINANCE_EXPENSE\", \"FINANCE_EXPENSE_YOY\", " +
            "\"FE_INTEREST_EXPENSE\", \"FE_INTEREST_INCOME\", \"INVEST_INCOME\", \"INVEST_INCOME_YOY\", " +
            "\"INVEST_JOINT_INCOME\", \"ASSET_DISPOSAL_INCOME\", \"ASSET_IMPAIRMENT_INCOME\", " +
            "\"CREDIT_IMPAIRMENT_INCOME\", \"OTHER_INCOME\", \"FAIRVALUE_CHANGE_INCOME\", " +
            "\"OPERATE_PROFIT\", \"OPERATE_PROFIT_YOY\", \"NONBUSINESS_INCOME\", " +
            "\"NONBUSINESS_EXPENSE\", \"TOTAL_PROFIT\", \"TOTAL_PROFIT_YOY\", \"INCOME_TAX\", " +
            "\"NETPROFIT\", \"NETPROFIT_YOY\", \"CONTINUED_NETPROFIT\", \"PARENT_NETPROFIT\", " +
            "\"MINORITY_INTEREST\", \"DEDUCT_PARENT_NETPROFIT\", \"BASIC_EPS\", \"DILUTED_EPS\", " +
            "\"OTHER_COMPRE_INCOME\", \"PARENT_OCI\", \"ABLE_OCI\", \"UNABLE_OCI\", " +
            "\"CONVERT_DIFF\", \"TOTAL_COMPRE_INCOME\", \"PARENT_TCI\", \"MINORITY_TCI\"";

    private static final RowMapper<StockIncomeStatement> ROW_MAPPER = (rs, rowNum) -> mapIncomeStatement(rs);

    private final JdbcTemplate jdbcTemplate;

    public IncomeStatementRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StockIncomeStatement> findBySecucode(String secucode) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM income_statement WHERE \"SECUCODE\" = ?",
                ROW_MAPPER, secucode);
    }

    public StockIncomeStatement findBySecucodeAndReportDate(String secucode, String reportDate) {
        return jdbcTemplate.queryForObject(
                "SELECT " + COLUMNS + " FROM income_statement WHERE \"SECUCODE\" = ? AND \"REPORT_DATE\" = ?",
                ROW_MAPPER, secucode, reportDate);
    }

    private static StockIncomeStatement mapIncomeStatement(java.sql.ResultSet rs) throws java.sql.SQLException {
        StockIncomeStatement s = new StockIncomeStatement();
        s.setSecucode(rs.getString("SECUCODE"));
        s.setSecurityCode(rs.getString("SECURITY_CODE"));
        s.setSecurityNameAbbr(rs.getString("SECURITY_NAME_ABBR"));
        s.setOrgCode(rs.getString("ORG_CODE"));
        s.setOrgType(rs.getString("ORG_TYPE"));
        s.setReportDate(rs.getString("REPORT_DATE"));
        s.setReportType(rs.getString("REPORT_TYPE"));
        s.setReportDateName(rs.getString("REPORT_DATE_NAME"));
        s.setSecurityTypeCode(rs.getString("SECURITY_TYPE_CODE"));
        s.setNoticeDate(rs.getString("NOTICE_DATE"));
        s.setUpdateDate(rs.getString("UPDATE_DATE"));
        s.setCurrency(rs.getString("CURRENCY"));
        s.setTotalOperateIncome(rs.getDouble("TOTAL_OPERATE_INCOME"));
        s.setTotalOperateIncomeYoY(rs.getDouble("TOTAL_OPERATE_INCOME_YOY"));
        s.setOperateIncome(rs.getDouble("OPERATE_INCOME"));
        s.setOperateIncomeYoY(rs.getDouble("OPERATE_INCOME_YOY"));
        s.setInterestIncome(rs.getDouble("INTEREST_INCOME"));
        s.setFeeCommissionIncome(rs.getDouble("FEE_COMMISSION_INCOME"));
        s.setOtherBusinessIncome(rs.getDouble("OTHER_BUSINESS_INCOME"));
        s.setTotalOperateCost(rs.getDouble("TOTAL_OPERATE_COST"));
        s.setTotalOperateCostYoY(rs.getDouble("TOTAL_OPERATE_COST_YOY"));
        s.setOperateCost(rs.getDouble("OPERATE_COST"));
        s.setOperateCostYoY(rs.getDouble("OPERATE_COST_YOY"));
        s.setInterestExpense(rs.getDouble("INTEREST_EXPENSE"));
        s.setFeeCommissionExpense(rs.getDouble("FEE_COMMISSION_EXPENSE"));
        s.setResearchExpense(rs.getDouble("RESEARCH_EXPENSE"));
        s.setResearchExpenseYoY(rs.getDouble("RESEARCH_EXPENSE_YOY"));
        s.setOperateTaxAdd(rs.getDouble("OPERATE_TAX_ADD"));
        s.setOperateTaxAddYoY(rs.getDouble("OPERATE_TAX_ADD_YOY"));
        s.setSaleExpense(rs.getDouble("SALE_EXPENSE"));
        s.setSaleExpenseYoY(rs.getDouble("SALE_EXPENSE_YOY"));
        s.setManageExpense(rs.getDouble("MANAGE_EXPENSE"));
        s.setManageExpenseYoY(rs.getDouble("MANAGE_EXPENSE_YOY"));
        s.setFinanceExpense(rs.getDouble("FINANCE_EXPENSE"));
        s.setFinanceExpenseYoY(rs.getDouble("FINANCE_EXPENSE_YOY"));
        s.setFeInterestExpense(rs.getDouble("FE_INTEREST_EXPENSE"));
        s.setFeInterestIncome(rs.getDouble("FE_INTEREST_INCOME"));
        s.setInvestIncome(rs.getDouble("INVEST_INCOME"));
        s.setInvestIncomeYoY(rs.getDouble("INVEST_INCOME_YOY"));
        s.setInvestJointIncome(rs.getDouble("INVEST_JOINT_INCOME"));
        s.setAssetDisposalIncome(rs.getDouble("ASSET_DISPOSAL_INCOME"));
        s.setAssetImpairmentIncome(rs.getDouble("ASSET_IMPAIRMENT_INCOME"));
        s.setCreditImpairmentIncome(rs.getDouble("CREDIT_IMPAIRMENT_INCOME"));
        s.setOtherIncome(rs.getDouble("OTHER_INCOME"));
        s.setFairvalueChangeIncome(rs.getDouble("FAIRVALUE_CHANGE_INCOME"));
        s.setOperateProfit(rs.getDouble("OPERATE_PROFIT"));
        s.setOperateProfitYoY(rs.getDouble("OPERATE_PROFIT_YOY"));
        s.setNonbusinessIncome(rs.getDouble("NONBUSINESS_INCOME"));
        s.setNonbusinessExpense(rs.getDouble("NONBUSINESS_EXPENSE"));
        s.setTotalProfit(rs.getDouble("TOTAL_PROFIT"));
        s.setTotalProfitYoY(rs.getDouble("TOTAL_PROFIT_YOY"));
        s.setIncomeTax(rs.getDouble("INCOME_TAX"));
        s.setNetprofit(rs.getDouble("NETPROFIT"));
        s.setNetprofitYoY(rs.getDouble("NETPROFIT_YOY"));
        s.setContinuedNetprofit(rs.getDouble("CONTINUED_NETPROFIT"));
        s.setParentNetprofit(rs.getDouble("PARENT_NETPROFIT"));
        s.setMinorityInterest(rs.getDouble("MINORITY_INTEREST"));
        s.setDeductParentNetprofit(rs.getDouble("DEDUCT_PARENT_NETPROFIT"));
        s.setBasicEps(rs.getDouble("BASIC_EPS"));
        s.setDilutedEps(rs.getDouble("DILUTED_EPS"));
        s.setOtherCompreIncome(rs.getDouble("OTHER_COMPRE_INCOME"));
        s.setParentOci(rs.getDouble("PARENT_OCI"));
        s.setAbleOci(rs.getDouble("ABLE_OCI"));
        s.setUnableOci(rs.getDouble("UNABLE_OCI"));
        s.setConvertDiff(rs.getDouble("CONVERT_DIFF"));
        s.setTotalCompreIncome(rs.getDouble("TOTAL_COMPRE_INCOME"));
        s.setParentTci(rs.getDouble("PARENT_TCI"));
        s.setMinorityTci(rs.getDouble("MINORITY_TCI"));

        return s;
    }
}
