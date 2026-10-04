package com.aitrade.stock.repository;

import com.aitrade.stock.domain.StockBalanceSheet;
import com.aitrade.tickflow.domain.TfStock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class BalanceSheetRepository {

    private static final String COLUMNS = "SECUCODE, SECURITY_CODE, SECURITY_NAME_ABBR, ORG_CODE, ORG_TYPE, REPORT_DATE, REPORT_TYPE, REPORT_DATE_NAME, SECURITY_TYPE_CODE, NOTICE_DATE, UPDATE_DATE, CURRENCY, MONETARYFUNDS, NOTE_ACCOUNTS_RECE, NOTE_RECE, ACCOUNTS_RECE, PREPAYMENT, OTHER_RECE, INVENTORY, OTHER_CURRENT_ASSET, TOTAL_CURRENT_ASSETS, LONG_EQUITY_INVEST, OTHER_EQUITY_INVEST, OTHER_NONCURRENT_FINASSET, INVEST_REALESTATE, FIXED_ASSET, CIP, USERIGHT_ASSET, INTANGIBLE_ASSET, LONG_PREPAID_EXPENSE, DEFER_TAX_ASSET, OTHER_NONCURRENT_ASSET, TOTAL_NONCURRENT_ASSETS, TOTAL_ASSETS, SHORT_LOAN, NOTE_ACCOUNTS_PAYABLE, NOTE_PAYABLE, ACCOUNTS_PAYABLE, ADVANCE_RECEIVABLES, CONTRACT_LIAB, STAFF_SALARY_PAYABLE, TAX_PAYABLE, OTHER_PAYABLE, DIVIDEND_PAYABLE, NONCURRENT_LIAB_1YEAR, OTHER_CURRENT_LIAB, TOTAL_CURRENT_LIAB, LONG_LOAN, LEASE_LIAB, LONG_PAYABLE, DEFER_TAX_LIAB, OTHER_NONCURRENT_LIAB, TOTAL_NONCURRENT_LIAB, TOTAL_LIABILITIES, SHARE_CAPITAL, CAPITAL_RESERVE, OTHER_COMPRE_INCOME, SURPLUS_RESERVE, UNASSIGN_RPOFIT, TOTAL_PARENT_EQUITY, MINORITY_EQUITY, TOTAL_EQUITY, TOTAL_LIAB_EQUITY, MONETARYFUNDS_YOY , TOTAL_ASSETS_YOY , TOTAL_LIABILITIES_YOY , TOTAL_EQUITY_YOY";

    private static final RowMapper<StockBalanceSheet> ROW_MAPPER = (rs, rowNum) -> mapBalanceSheet(rs);

    private final JdbcTemplate jdbcTemplate;

    public BalanceSheetRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StockBalanceSheet> findBySecucode(String secucode) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM balance_sheet WHERE secucode = ?",
                ROW_MAPPER, secucode);
    }

    public StockBalanceSheet findBySecucodeAndReportDate(String secucode, String reportDate) {
        return jdbcTemplate.queryForObject(
                "SELECT " + COLUMNS + " FROM balance_sheet WHERE secucode = ? AND report_date = ?",
                ROW_MAPPER, secucode, reportDate);
    }

    private static StockBalanceSheet mapBalanceSheet(ResultSet rs) throws SQLException {
        StockBalanceSheet s = new StockBalanceSheet();
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
        s.setMonetaryfunds(rs.getDouble("MONETARYFUNDS"));
        s.setNoteAccountsRece(rs.getDouble("NOTE_ACCOUNTS_RECE"));
        s.setNoteRece(rs.getDouble("NOTE_RECE"));
        s.setAccountsRece(rs.getDouble("ACCOUNTS_RECE"));
        s.setPrepayment(rs.getDouble("PREPAYMENT"));
        s.setOtherRece(rs.getDouble("OTHER_RECE"));
        s.setInventory(rs.getDouble("INVENTORY"));
        s.setOtherCurrentAsset(rs.getDouble("OTHER_CURRENT_ASSET"));
        s.setTotalCurrentAssets(rs.getDouble("TOTAL_CURRENT_ASSETS"));
        s.setLongEquityInvest(rs.getDouble("LONG_EQUITY_INVEST"));
        s.setOtherEquityInvest(rs.getDouble("OTHER_EQUITY_INVEST"));
        s.setOtherNoncurrentFinasst(rs.getDouble("OTHER_NONCURRENT_FINASSET"));
        s.setInvestRealestate(rs.getDouble("INVEST_REALESTATE"));
        s.setFixedAsset(rs.getDouble("FIXED_ASSET"));
        s.setCip(rs.getDouble("CIP"));
        s.setUserightAsset(rs.getDouble("USERIGHT_ASSET"));
        s.setIntangibleAsset(rs.getDouble("INTANGIBLE_ASSET"));
        s.setLongPrepaidExpense(rs.getDouble("LONG_PREPAID_EXPENSE"));
        s.setDeferTaxAsset(rs.getDouble("DEFER_TAX_ASSET"));
        s.setOtherNoncurrentAsset(rs.getDouble("OTHER_NONCURRENT_ASSET"));
        s.setTotalNoncurrentAssets(rs.getDouble("TOTAL_NONCURRENT_ASSETS"));
        s.setTotalAssets(rs.getDouble("TOTAL_ASSETS"));
        s.setShortLoan(rs.getDouble("SHORT_LOAN"));
        s.setNoteAccountsPayable(rs.getDouble("NOTE_ACCOUNTS_PAYABLE"));
        s.setNotePayable(rs.getDouble("NOTE_PAYABLE"));
        s.setAccountsPayable(rs.getDouble("ACCOUNTS_PAYABLE"));
        s.setAdvanceReceivables(rs.getDouble("ADVANCE_RECEIVABLES"));
        s.setContractLiab(rs.getDouble("CONTRACT_LIAB"));
        s.setStaffSalaryPayable(rs.getDouble("STAFF_SALARY_PAYABLE"));
        s.setTaxPayable(rs.getDouble("TAX_PAYABLE"));
        s.setOtherPayable(rs.getDouble("OTHER_PAYABLE"));
        s.setDividendPayable(rs.getDouble("DIVIDEND_PAYABLE"));
        s.setNoncurrentLiab1year(rs.getDouble("NONCURRENT_LIAB_1YEAR"));
        s.setOtherCurrentLiab(rs.getDouble("OTHER_CURRENT_LIAB"));
        s.setTotalCurrentLiab(rs.getDouble("TOTAL_CURRENT_LIAB"));
        s.setLongLoan(rs.getDouble("LONG_LOAN"));
        s.setLeaseLiab(rs.getDouble("LEASE_LIAB"));
        s.setLongPayable(rs.getDouble("LONG_PAYABLE"));
        s.setDeferTaxLiab(rs.getDouble("DEFER_TAX_LIAB"));
        s.setOtherNoncurrentLiab(rs.getDouble("OTHER_NONCURRENT_LIAB"));
        s.setTotalNoncurrentLiab(rs.getDouble("TOTAL_NONCURRENT_LIAB"));
        s.setTotalLiabilities(rs.getDouble("TOTAL_LIABILITIES"));
        s.setShareCapital(rs.getDouble("SHARE_CAPITAL"));
        s.setCapitalReserve(rs.getDouble("CAPITAL_RESERVE"));
        s.setOtherCompreIncome(rs.getDouble("OTHER_COMPRE_INCOME"));
        s.setSurplusReserve(rs.getDouble("SURPLUS_RESERVE"));
        s.setUnassignRprofit(rs.getDouble("UNASSIGN_RPOFIT"));
        s.setTotalParentEquity(rs.getDouble("TOTAL_PARENT_EQUITY"));
        s.setMinorityEquity(rs.getDouble("MINORITY_EQUITY"));
        s.setTotalEquity(rs.getDouble("TOTAL_EQUITY"));
        s.setTotalLiabEquity(rs.getDouble("TOTAL_LIAB_EQUITY"));
        s.setMonetaryfundsYoy(rs.getDouble("MONETARYFUNDS_YOY"));
        s.setTotalAssetsYoy(rs.getDouble("TOTAL_ASSETS_YOY"));
        s.setTotalLiabilitiesYoy(rs.getDouble("TOTAL_LIABILITIES_YOY"));
        s.setTotalEquityYoy(rs.getDouble("TOTAL_EQUITY_YOY"));
        return s;
    }
}
