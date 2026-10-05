package com.aitrade.stock.repository;

import com.aitrade.stock.domain.StockCashFlowStatement;
import com.aitrade.stock.domain.StockIncomeStatement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CashFlowStatementRepository {
    private static final String COLUMNS =
            "\"SECUCODE\", \"SECURITY_CODE\", \"SECURITY_NAME_ABBR\", \"ORG_CODE\", \"ORG_TYPE\", " +
            "\"REPORT_DATE\", \"REPORT_TYPE\", \"REPORT_DATE_NAME\", \"SECURITY_TYPE_CODE\", " +
            "\"NOTICE_DATE\", \"UPDATE_DATE\", \"CURRENCY\", " +
            "\"SALES_SERVICES\", \"DEPOSIT_INTERBANK_ADD\", \"LOAN_PBC_ADD\", \"OFI_BF_ADD\", " +
            "\"RECEIVE_ORIGIC_PREMIUM\", \"RECEIVE_REINSURE_NET\", \"INSURED_INVEST_ADD\", " +
            "\"DISPOSAL_TFA_ADD\", \"RECEIVE_INTEREST_COMMISSION\", \"BORROW_FUND_ADD\", " +
            "\"LOAN_ADVANCE_REDUCE\", \"REPO_BUSINESS_ADD\", \"RECEIVE_TAX_REFUND\", " +
            "\"RECEIVE_OTHER_OPERATE\", \"OPERATE_INFLOW_OTHER\", \"OPERATE_INFLOW_BALANCE\", " +
            "\"TOTAL_OPERATE_INFLOW\", \"BUY_SERVICES\", \"LOAN_ADVANCE_ADD\", \"PBC_INTERBANK_ADD\", " +
            "\"PAY_ORIGIC_COMPENSATE\", \"PAY_INTEREST_COMMISSION\", \"PAY_POLICY_BONUS\", " +
            "\"PAY_STAFF_CASH\", \"PAY_ALL_TAX\", \"PAY_OTHER_OPERATE\", " +
            "\"OPERATE_OUTFLOW_OTHER\", \"OPERATE_OUTFLOW_BALANCE\", \"TOTAL_OPERATE_OUTFLOW\", " +
            "\"OPERATE_NETCASH_OTHER\", \"OPERATE_NETCASH_BALANCE\", \"NETCASH_OPERATE\", " +
            "\"WITHDRAW_INVEST\", \"RECEIVE_INVEST_INCOME\", \"DISPOSAL_LONG_ASSET\", " +
            "\"DISPOSAL_SUBSIDIARY_OTHER\", \"REDUCE_PLEDGE_TIMEDEPOSITS\", \"RECEIVE_OTHER_INVEST\", " +
            "\"INVEST_INFLOW_OTHER\", \"INVEST_INFLOW_BALANCE\", \"TOTAL_INVEST_INFLOW\", " +
            "\"CONSTRUCT_LONG_ASSET\", \"INVEST_PAY_CASH\", \"PLEDGE_LOAN_ADD\", " +
            "\"OBTAIN_SUBSIDIARY_OTHER\", \"ADD_PLEDGE_TIMEDEPOSITS\", \"PAY_OTHER_INVEST\", " +
            "\"INVEST_OUTFLOW_OTHER\", \"INVEST_OUTFLOW_BALANCE\", \"TOTAL_INVEST_OUTFLOW\", " +
            "\"INVEST_NETCASH_OTHER\", \"INVEST_NETCASH_BALANCE\", \"NETCASH_INVEST\", " +
            "\"ACCEPT_INVEST_CASH\", \"SUBSIDIARY_ACCEPT_INVEST\", \"RECEIVE_LOAN_CASH\", " +
            "\"ISSUE_BOND\", \"RECEIVE_OTHER_FINANCE\", \"FINANCE_INFLOW_OTHER\", " +
            "\"FINANCE_INFLOW_BALANCE\", \"TOTAL_FINANCE_INFLOW\", \"PAY_DEBT_CASH\", " +
            "\"ASSIGN_DIVIDEND_PORFIT\", \"SUBSIDIARY_PAY_DIVIDEND\", \"BUY_SUBSIDIARY_EQUITY\", " +
            "\"PAY_OTHER_FINANCE\", \"SUBSIDIARY_REDUCE_CASH\", \"FINANCE_OUTFLOW_OTHER\", " +
            "\"FINANCE_OUTFLOW_BALANCE\", \"TOTAL_FINANCE_OUTFLOW\", \"FINANCE_NETCASH_OTHER\", " +
            "\"FINANCE_NETCASH_BALANCE\", \"NETCASH_FINANCE\", \"RATE_CHANGE_EFFECT\", " +
            "\"CCE_ADD_OTHER\", \"CCE_ADD_BALANCE\", \"CCE_ADD\", \"BEGIN_CCE\", " +
            "\"END_CCE_OTHER\", \"END_CCE_BALANCE\", \"END_CCE\", " +
            "\"NETPROFIT\", \"ASSET_IMPAIRMENT\", \"FA_IR_DEPR\", \"OILGAS_BIOLOGY_DEPR\", " +
            "\"IR_DEPR\", \"IA_AMORTIZE\", \"LPE_AMORTIZE\", \"DEFER_INCOME_AMORTIZE\", " +
            "\"PREPAID_EXPENSE_REDUCE\", \"ACCRUED_EXPENSE_ADD\", \"DISPOSAL_LONGASSET_LOSS\", " +
            "\"FA_SCRAP_LOSS\", \"FAIRVALUE_CHANGE_LOSS\", \"FINANCE_EXPENSE\", \"INVEST_LOSS\", " +
            "\"DEFER_TAX\", \"DT_ASSET_REDUCE\", \"DT_LIAB_ADD\", \"PREDICT_LIAB_ADD\", " +
            "\"INVENTORY_REDUCE\", \"OPERATE_RECE_REDUCE\", \"OPERATE_PAYABLE_ADD\", \"OTHER\", " +
            "\"OPERATE_NETCASH_OTHERNOTE\", \"OPERATE_NETCASH_BALANCENOTE\", \"NETCASH_OPERATENOTE\", " +
            "\"DEBT_TRANSFER_CAPITAL\", \"CONVERT_BOND_1YEAR\", \"FINLEASE_OBTAIN_FA\", " +
            "\"UNINVOLVE_INVESTFIN_OTHER\", \"END_CASH\", \"BEGIN_CASH\", " +
            "\"END_CASH_EQUIVALENTS\", \"BEGIN_CASH_EQUIVALENTS\", \"CCE_ADD_OTHERNOTE\", " +
            "\"CCE_ADD_BALANCENOTE\", \"CCE_ADDNOTE\", \"OPINION_TYPE\", \"OSOPINION_TYPE\", " +
            "\"MINORITY_INTEREST\", \"USERIGHT_ASSET_AMORTIZE\"";

    private static final RowMapper<StockCashFlowStatement> ROW_MAPPER = (rs, rowNum) -> mapCashFlowStatement(rs);

    private final JdbcTemplate jdbcTemplate;

    public CashFlowStatementRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StockCashFlowStatement> findBySecucode(String SECUCODE) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM cash_flow_statement WHERE \"SECUCODE\" = ?",
                ROW_MAPPER, SECUCODE);
    }

    public StockCashFlowStatement findBySecucodeAndReportDate(String SECUCODE, String reportDate) {
        return jdbcTemplate.queryForObject(
                "SELECT " + COLUMNS + " FROM cash_flow_statement WHERE \"SECUCODE\" = ? AND \"REPORT_DATE\" = ?",
                ROW_MAPPER, SECUCODE, reportDate);
    }

    private static StockCashFlowStatement mapCashFlowStatement(java.sql.ResultSet rs) throws java.sql.SQLException {
        StockCashFlowStatement s = new StockCashFlowStatement();
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
        s.setSalesServices(rs.getDouble("SALES_SERVICES"));
        s.setDepositInterbankAdd(rs.getDouble("DEPOSIT_INTERBANK_ADD"));
        s.setLoanPbcAdd(rs.getDouble("LOAN_PBC_ADD"));
        s.setOfiBfAdd(rs.getDouble("OFI_BF_ADD"));
        s.setReceiveOrigicPremium(rs.getDouble("RECEIVE_ORIGIC_PREMIUM"));
        s.setReceiveReinsureNet(rs.getDouble("RECEIVE_REINSURE_NET"));
        s.setInsuredInvestAdd(rs.getDouble("INSURED_INVEST_ADD"));
        s.setDisposalTfaAdd(rs.getDouble("DISPOSAL_TFA_ADD"));
        s.setReceiveInterestCommission(rs.getDouble("RECEIVE_INTEREST_COMMISSION"));
        s.setBorrowFundAdd(rs.getDouble("BORROW_FUND_ADD"));
        s.setLoanAdvanceReduce(rs.getDouble("LOAN_ADVANCE_REDUCE"));
        s.setRepoBusinessAdd(rs.getDouble("REPO_BUSINESS_ADD"));
        s.setReceiveTaxRefund(rs.getDouble("RECEIVE_TAX_REFUND"));
        s.setReceiveOtherOperate(rs.getDouble("RECEIVE_OTHER_OPERATE"));
        s.setOperateInflowOther(rs.getDouble("OPERATE_INFLOW_OTHER"));
        s.setOperateInflowBalance(rs.getDouble("OPERATE_INFLOW_BALANCE"));
        s.setTotalOperateInflow(rs.getDouble("TOTAL_OPERATE_INFLOW"));
        s.setBuyServices(rs.getDouble("BUY_SERVICES"));
        s.setLoanAdvanceAdd(rs.getDouble("LOAN_ADVANCE_ADD"));
        s.setPbcInterbankAdd(rs.getDouble("PBC_INTERBANK_ADD"));
        s.setPayOrigicCompensate(rs.getDouble("PAY_ORIGIC_COMPENSATE"));
        s.setPayInterestCommission(rs.getDouble("PAY_INTEREST_COMMISSION"));
        s.setPayPolicyBonus(rs.getDouble("PAY_POLICY_BONUS"));
        s.setPayStaffCash(rs.getDouble("PAY_STAFF_CASH"));
        s.setPayAllTax(rs.getDouble("PAY_ALL_TAX"));
        s.setPayOtherOperate(rs.getDouble("PAY_OTHER_OPERATE"));
        s.setOperateOutflowOther(rs.getDouble("OPERATE_OUTFLOW_OTHER"));
        s.setOperateOutflowBalance(rs.getDouble("OPERATE_OUTFLOW_BALANCE"));
        s.setTotalOperateOutflow(rs.getDouble("TOTAL_OPERATE_OUTFLOW"));
        s.setOperateNetcashOther(rs.getDouble("OPERATE_NETCASH_OTHER"));
        s.setOperateNetcashBalance(rs.getDouble("OPERATE_NETCASH_BALANCE"));
        s.setNetcashOperate(rs.getDouble("NETCASH_OPERATE"));
        s.setWithdrawInvest(rs.getDouble("WITHDRAW_INVEST"));
        s.setReceiveInvestIncome(rs.getDouble("RECEIVE_INVEST_INCOME"));
        s.setDisposalLongAsset(rs.getDouble("DISPOSAL_LONG_ASSET"));
        s.setDisposalSubsidiaryOther(rs.getDouble("DISPOSAL_SUBSIDIARY_OTHER"));
        s.setReducePledgeTimedeposits(rs.getDouble("REDUCE_PLEDGE_TIMEDEPOSITS"));
        s.setReceiveOtherInvest(rs.getDouble("RECEIVE_OTHER_INVEST"));
        s.setInvestInflowOther(rs.getDouble("INVEST_INFLOW_OTHER"));
        s.setInvestInflowBalance(rs.getDouble("INVEST_INFLOW_BALANCE"));
        s.setTotalInvestInflow(rs.getDouble("TOTAL_INVEST_INFLOW"));
        s.setConstructLongAsset(rs.getDouble("CONSTRUCT_LONG_ASSET"));
        s.setInvestPayCash(rs.getDouble("INVEST_PAY_CASH"));
        s.setPledgeLoanAdd(rs.getDouble("PLEDGE_LOAN_ADD"));
        s.setObtainSubsidiaryOther(rs.getDouble("OBTAIN_SUBSIDIARY_OTHER"));
        s.setAddPledgeTimedeposits(rs.getDouble("ADD_PLEDGE_TIMEDEPOSITS"));
        s.setPayOtherInvest(rs.getDouble("PAY_OTHER_INVEST"));
        s.setInvestOutflowOther(rs.getDouble("INVEST_OUTFLOW_OTHER"));
        s.setInvestOutflowBalance(rs.getDouble("INVEST_OUTFLOW_BALANCE"));
        s.setTotalInvestOutflow(rs.getDouble("TOTAL_INVEST_OUTFLOW"));
        s.setInvestNetcashOther(rs.getDouble("INVEST_NETCASH_OTHER"));
        s.setInvestNetcashBalance(rs.getDouble("INVEST_NETCASH_BALANCE"));
        s.setNetcashInvest(rs.getDouble("NETCASH_INVEST"));
        s.setAcceptInvestCash(rs.getDouble("ACCEPT_INVEST_CASH"));
        s.setSubsidiaryAcceptInvest(rs.getDouble("SUBSIDIARY_ACCEPT_INVEST"));
        s.setReceiveLoanCash(rs.getDouble("RECEIVE_LOAN_CASH"));
        s.setIssueBond(rs.getDouble("ISSUE_BOND"));
        s.setReceiveOtherFinance(rs.getDouble("RECEIVE_OTHER_FINANCE"));
        s.setFinanceInflowOther(rs.getDouble("FINANCE_INFLOW_OTHER"));
        s.setFinanceInflowBalance(rs.getDouble("FINANCE_INFLOW_BALANCE"));
        s.setTotalFinanceInflow(rs.getDouble("TOTAL_FINANCE_INFLOW"));
        s.setPayDebtCash(rs.getDouble("PAY_DEBT_CASH"));
        s.setAssignDividendPorfit(rs.getDouble("ASSIGN_DIVIDEND_PORFIT"));
        s.setSubsidiaryPayDividend(rs.getDouble("SUBSIDIARY_PAY_DIVIDEND"));
        s.setBuySubsidiaryEquity(rs.getDouble("BUY_SUBSIDIARY_EQUITY"));
        s.setPayOtherFinance(rs.getDouble("PAY_OTHER_FINANCE"));
        s.setSubsidiaryReduceCash(rs.getDouble("SUBSIDIARY_REDUCE_CASH"));
        s.setFinanceOutflowOther(rs.getDouble("FINANCE_OUTFLOW_OTHER"));
        s.setFinanceOutflowBalance(rs.getDouble("FINANCE_OUTFLOW_BALANCE"));
        s.setTotalFinanceOutflow(rs.getDouble("TOTAL_FINANCE_OUTFLOW"));
        s.setFinanceNetcashOther(rs.getDouble("FINANCE_NETCASH_OTHER"));
        s.setFinanceNetcashBalance(rs.getDouble("FINANCE_NETCASH_BALANCE"));
        s.setNetcashFinance(rs.getDouble("NETCASH_FINANCE"));
        s.setRateChangeEffect(rs.getDouble("RATE_CHANGE_EFFECT"));
        s.setCceAddOther(rs.getDouble("CCE_ADD_OTHER"));
        s.setCceAddBalance(rs.getDouble("CCE_ADD_BALANCE"));
        s.setCceAdd(rs.getDouble("CCE_ADD"));
        s.setBeginCce(rs.getDouble("BEGIN_CCE"));
        s.setEndCceOther(rs.getDouble("END_CCE_OTHER"));
        s.setEndCceBalance(rs.getDouble("END_CCE_BALANCE"));
        s.setEndCce(rs.getDouble("END_CCE"));
        s.setNetprofit(rs.getDouble("NETPROFIT"));
        s.setAssetImpairment(rs.getDouble("ASSET_IMPAIRMENT"));
        s.setFaIrDepr(rs.getDouble("FA_IR_DEPR"));
        s.setOilgasBiologyDepr(rs.getDouble("OILGAS_BIOLOGY_DEPR"));
        s.setIrDepr(rs.getDouble("IR_DEPR"));
        s.setIaAmortize(rs.getDouble("IA_AMORTIZE"));
        s.setLpeAmortize(rs.getDouble("LPE_AMORTIZE"));
        s.setDeferIncomeAmortize(rs.getDouble("DEFER_INCOME_AMORTIZE"));
        s.setPrepaidExpenseReduce(rs.getDouble("PREPAID_EXPENSE_REDUCE"));
        s.setAccruedExpenseAdd(rs.getDouble("ACCRUED_EXPENSE_ADD"));
        s.setDisposalLongassetLoss(rs.getDouble("DISPOSAL_LONGASSET_LOSS"));
        s.setFaScrapLoss(rs.getDouble("FA_SCRAP_LOSS"));
        s.setFairvalueChangeLoss(rs.getDouble("FAIRVALUE_CHANGE_LOSS"));
        s.setFinanceExpense(rs.getDouble("FINANCE_EXPENSE"));
        s.setInvestLoss(rs.getDouble("INVEST_LOSS"));
        s.setDeferTax(rs.getDouble("DEFER_TAX"));
        s.setDtAssetReduce(rs.getDouble("DT_ASSET_REDUCE"));
        s.setDtLiabAdd(rs.getDouble("DT_LIAB_ADD"));
        s.setPredictLiabAdd(rs.getDouble("PREDICT_LIAB_ADD"));
        s.setInventoryReduce(rs.getDouble("INVENTORY_REDUCE"));
        s.setOperateReceReduce(rs.getDouble("OPERATE_RECE_REDUCE"));
        s.setOperatePayableAdd(rs.getDouble("OPERATE_PAYABLE_ADD"));
        s.setOther(rs.getDouble("OTHER"));
        s.setOperateNetcashOthernote(rs.getDouble("OPERATE_NETCASH_OTHERNOTE"));
        s.setOperateNetcashBalancenote(rs.getDouble("OPERATE_NETCASH_BALANCENOTE"));
        s.setNetcashOperatenote(rs.getDouble("NETCASH_OPERATENOTE"));
        s.setDebtTransferCapital(rs.getDouble("DEBT_TRANSFER_CAPITAL"));
        s.setConvertBond1year(rs.getDouble("CONVERT_BOND_1YEAR"));
        s.setFinleaseObtainFa(rs.getDouble("FINLEASE_OBTAIN_FA"));
        s.setUninvolveInvestfinOther(rs.getDouble("UNINVOLVE_INVESTFIN_OTHER"));
        s.setEndCash(rs.getDouble("END_CASH"));
        s.setBeginCash(rs.getDouble("BEGIN_CASH"));
        s.setEndCashEquivalents(rs.getDouble("END_CASH_EQUIVALENTS"));
        s.setBeginCashEquivalents(rs.getDouble("BEGIN_CASH_EQUIVALENTS"));
        s.setCceAddOthernote(rs.getDouble("CCE_ADD_OTHERNOTE"));
        s.setCceAddBalancenote(rs.getDouble("CCE_ADD_BALANCENOTE"));
        s.setCceAddnote(rs.getDouble("CCE_ADDNOTE"));
        s.setOpinionType(rs.getString("OPINION_TYPE"));
        s.setOsopinionType(rs.getString("OSOPINION_TYPE"));
        s.setMinorityInterest(rs.getDouble("MINORITY_INTEREST"));
        s.setUserightAssetAmortize(rs.getDouble("USERIGHT_ASSET_AMORTIZE"));
        return s;
    }
}
