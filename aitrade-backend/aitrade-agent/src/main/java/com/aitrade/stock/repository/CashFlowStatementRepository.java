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
    private static final String COLUMNS = "SECUCODE, SECURITY_CODE, SECURITY_NAME_ABBR, ORG_CODE, ORG_TYPE, REPORT_DATE, REPORT_TYPE, REPORT_DATE_NAME, SECURITY_TYPE_CODE, NOTICE_DATE, UPDATE_DATE, CURRENCY, SALES_SERVICES, DEPOSIT_INTERBANK_ADD, LOAN_PBC_ADD, OFI_BF_ADD, RECEIVE_ORIGIC_PREMIUM, RECEIVE_REINSURE_NET, INSURED_INVEST_ADD, DISPOSAL_TFA_ADD, RECEIVE_INTEREST_COMMISSION, BORROW_FUND_ADD, LOAN_ADVANCE_REDUCE, REPO_BUSINESS_ADD, RECEIVE_TAX_REFUND, RECEIVE_OTHER_OPERATE, OPERATE_INFLOW_OTHER, OPERATE_INFLOW_BALANCE, TOTAL_OPERATE_INFLOW, BUY_SERVICES, LOAN_ADVANCE_ADD, PBC_INTERBANK_ADD, PAY_ORIGIC_COMPENSATE, PAY_INTEREST_COMMISSION, PAY_POLICY_BONUS, PAY_STAFF_CASH, PAY_ALL_TAX, PAY_OTHER_OPERATE, OPERATE_OUTFLOW_OTHER, OPERATE_OUTFLOW_BALANCE, TOTAL_OPERATE_OUTFLOW, OPERATE_NETCASH_OTHER, OPERATE_NETCASH_BALANCE, NETCASH_OPERATE, WITHDRAW_INVEST, RECEIVE_INVEST_INCOME, DISPOSAL_LONG_ASSET, DISPOSAL_SUBSIDIARY_OTHER, REDUCE_PLEDGE_TIMEDEPOSITS, RECEIVE_OTHER_INVEST, INVEST_INFLOW_OTHER, INVEST_INFLOW_BALANCE, TOTAL_INVEST_INFLOW, CONSTRUCT_LONG_ASSET, INVEST_PAY_CASH, PLEDGE_LOAN_ADD, OBTAIN_SUBSIDIARY_OTHER, ADD_PLEDGE_TIMEDEPOSITS, PAY_OTHER_INVEST, INVEST_OUTFLOW_OTHER, INVEST_OUTFLOW_BALANCE, TOTAL_INVEST_OUTFLOW, INVEST_NETCASH_OTHER, INVEST_NETCASH_BALANCE, NETCASH_INVEST, ACCEPT_INVEST_CASH, SUBSIDIARY_ACCEPT_INVEST, RECEIVE_LOAN_CASH, ISSUE_BOND, RECEIVE_OTHER_FINANCE, FINANCE_INFLOW_OTHER, FINANCE_INFLOW_BALANCE, TOTAL_FINANCE_INFLOW, PAY_DEBT_CASH, ASSIGN_DIVIDEND_PORFIT, SUBSIDIARY_PAY_DIVIDEND, BUY_SUBSIDIARY_EQUITY, PAY_OTHER_FINANCE, SUBSIDIARY_REDUCE_CASH, FINANCE_OUTFLOW_OTHER, FINANCE_OUTFLOW_BALANCE, TOTAL_FINANCE_OUTFLOW, FINANCE_NETCASH_OTHER, FINANCE_NETCASH_BALANCE, NETCASH_FINANCE, RATE_CHANGE_EFFECT, CCE_ADD_OTHER, CCE_ADD_BALANCE, CCE_ADD, BEGIN_CCE, END_CCE_OTHER, END_CCE_BALANCE, END_CCE, NETPROFIT, ASSET_IMPAIRMENT, FA_IR_DEPR, OILGAS_BIOLOGY_DEPR, IR_DEPR, IA_AMORTIZE, LPE_AMORTIZE, DEFER_INCOME_AMORTIZE, PREPAID_EXPENSE_REDUCE, ACCRUED_EXPENSE_ADD, DISPOSAL_LONGASSET_LOSS, FA_SCRAP_LOSS, FAIRVALUE_CHANGE_LOSS, FINANCE_EXPENSE, INVEST_LOSS, DEFER_TAX, DT_ASSET_REDUCE, DT_LIAB_ADD, PREDICT_LIAB_ADD, INVENTORY_REDUCE, OPERATE_RECE_REDUCE, OPERATE_PAYABLE_ADD, OTHER, OPERATE_NETCASH_OTHERNOTE, OPERATE_NETCASH_BALANCENOTE, NETCASH_OPERATENOTE, DEBT_TRANSFER_CAPITAL, CONVERT_BOND_1YEAR, FINLEASE_OBTAIN_FA, UNINVOLVE_INVESTFIN_OTHER, END_CASH, BEGIN_CASH, END_CASH_EQUIVALENTS, BEGIN_CASH_EQUIVALENTS, CCE_ADD_OTHERNOTE, CCE_ADD_BALANCENOTE, CCE_ADDNOTE, OPINION_TYPE, OSOPINION_TYPE,MINORITY_INTEREST, USERIGHT_ASSET_AMORTIZE";

    private static final RowMapper<StockCashFlowStatement> ROW_MAPPER = (rs, rowNum) -> mapCashFlowStatement(rs);

    private final JdbcTemplate jdbcTemplate;

    public CashFlowStatementRepository(
            @Qualifier("timescaleJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StockCashFlowStatement> findBySecucode(String secucode) {
        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM cash_flow_statement WHERE secucode = ?",
                ROW_MAPPER, secucode);
    }

    public StockCashFlowStatement findBySecucodeAndReportDate(String secucode, String reportDate) {
        return jdbcTemplate.queryForObject(
                "SELECT " + COLUMNS + " FROM cash_flow_statement WHERE secucode = ? AND report_date = ?",
                ROW_MAPPER, secucode, reportDate);
    }

    private static StockCashFlowStatement mapCashFlowStatement(java.sql.ResultSet rs) throws java.sql.SQLException {
        StockCashFlowStatement s = new StockCashFlowStatement();
        s.setSecucode(rs.getString("secucode"));
        s.setSecurityCode(rs.getString("security_code"));
        s.setSecurityNameAbbr(rs.getString("security_name_abbr"));
        s.setOrgCode(rs.getString("org_code"));
        s.setOrgType(rs.getString("org_type"));
        s.setReportDate(rs.getString("report_date"));
        s.setReportType(rs.getString("report_type"));
        s.setReportDateName(rs.getString("report_date_name"));
        s.setSecurityTypeCode(rs.getString("security_type_code"));
        s.setNoticeDate(rs.getString("notice_date"));
        s.setUpdateDate(rs.getString("update_date"));
        s.setCurrency(rs.getString("currency"));
        s.setSalesServices(rs.getDouble("sales_services"));
        s.setDepositInterbankAdd(rs.getDouble("deposit_interbank_add"));
        s.setLoanPbcAdd(rs.getDouble("loan_pbc_add"));
        s.setOfiBfAdd(rs.getDouble("ofi_bf_add"));
        s.setReceiveOrigicPremium(rs.getDouble("receive_origic_premium"));
        s.setReceiveReinsureNet(rs.getDouble("receive_reinsure_net"));
        s.setInsuredInvestAdd(rs.getDouble("insured_invest_add"));
        s.setDisposalTfaAdd(rs.getDouble("disposal_tfa_add"));
        s.setReceiveInterestCommission(rs.getDouble("receive_interest_commission"));
        s.setBorrowFundAdd(rs.getDouble("borrow_fund_add"));
        s.setLoanAdvanceReduce(rs.getDouble("loan_advance_reduce"));
        s.setRepoBusinessAdd(rs.getDouble("repo_business_add"));
        s.setReceiveTaxRefund(rs.getDouble("receive_tax_refund"));
        s.setReceiveOtherOperate(rs.getDouble("receive_other_operate"));
        s.setOperateInflowOther(rs.getDouble("operate_inflow_other"));
        s.setOperateInflowBalance(rs.getDouble("operate_inflow_balance"));
        s.setTotalOperateInflow(rs.getDouble("total_operate_inflow"));
        s.setBuyServices(rs.getDouble("buy_services"));
        s.setLoanAdvanceAdd(rs.getDouble("loan_advance_add"));
        s.setPbcInterbankAdd(rs.getDouble("pbc_interbank_add"));
        s.setPayOrigicCompensate(rs.getDouble("pay_origic_compensate"));
        s.setPayInterestCommission(rs.getDouble("pay_interest_commission"));
        s.setPayPolicyBonus(rs.getDouble("pay_policy_bonus"));
        s.setPayStaffCash(rs.getDouble("pay_staff_cash"));
        s.setPayAllTax(rs.getDouble("pay_all_tax"));
        s.setPayOtherOperate(rs.getDouble("pay_other_operate"));
        s.setOperateOutflowOther(rs.getDouble("operate_outflow_other"));
        s.setOperateOutflowBalance(rs.getDouble("operate_outflow_balance"));
        s.setTotalOperateOutflow(rs.getDouble("total_operate_outflow"));
        s.setOperateNetcashOther(rs.getDouble("operate_netcash_other"));
        s.setOperateNetcashBalance(rs.getDouble("operate_netcash_balance"));
        s.setNetcashOperate(rs.getDouble("netcash_operate"));
        s.setWithdrawInvest(rs.getDouble("withdraw_invest"));
        s.setReceiveInvestIncome(rs.getDouble("receive_invest_income"));
        s.setDisposalLongAsset(rs.getDouble("disposal_long_asset"));
        s.setDisposalSubsidiaryOther(rs.getDouble("disposal_subsidiary_other"));
        s.setReducePledgeTimedeposits(rs.getDouble("reduce_pledge_timedeposits"));
        s.setReceiveOtherInvest(rs.getDouble("receive_other_invest"));
        s.setInvestInflowOther(rs.getDouble("invest_inflow_other"));
        s.setInvestInflowBalance(rs.getDouble("invest_inflow_balance"));
        s.setTotalInvestInflow(rs.getDouble("total_invest_inflow"));
        s.setConstructLongAsset(rs.getDouble("construct_long_asset"));
        s.setInvestPayCash(rs.getDouble("invest_pay_cash"));
        s.setPledgeLoanAdd(rs.getDouble("pledge_loan_add"));
        s.setObtainSubsidiaryOther(rs.getDouble("obtain_subsidiary_other"));
        s.setAddPledgeTimedeposits(rs.getDouble("add_pledge_timedeposits"));
        s.setPayOtherInvest(rs.getDouble("pay_other_invest"));
        s.setInvestOutflowOther(rs.getDouble("invest_outflow_other"));
        s.setInvestOutflowBalance(rs.getDouble("invest_outflow_balance"));
        s.setTotalInvestOutflow(rs.getDouble("total_invest_outflow"));
        s.setInvestNetcashOther(rs.getDouble("invest_netcash_other"));
        s.setInvestNetcashBalance(rs.getDouble("invest_netcash_balance"));
        s.setNetcashInvest(rs.getDouble("netcash_invest"));
        s.setAcceptInvestCash(rs.getDouble("accept_invest_cash"));
        s.setSubsidiaryAcceptInvest(rs.getDouble("subsidiary_accept_invest"));
        s.setReceiveLoanCash(rs.getDouble("receive_loan_cash"));
        s.setIssueBond(rs.getDouble("issue_bond"));
        s.setReceiveOtherFinance(rs.getDouble("receive_other_finance"));
        s.setFinanceInflowOther(rs.getDouble("finance_inflow_other"));
        s.setFinanceInflowBalance(rs.getDouble("finance_inflow_balance"));
        s.setTotalFinanceInflow(rs.getDouble("total_finance_inflow"));
        s.setPayDebtCash(rs.getDouble("pay_debt_cash"));
        s.setAssignDividendPorfit(rs.getDouble("assign_dividend_porfit"));
        s.setSubsidiaryPayDividend(rs.getDouble("subsidiary_pay_dividend"));
        s.setBuySubsidiaryEquity(rs.getDouble("buy_subsidiary_equity"));
        s.setPayOtherFinance(rs.getDouble("pay_other_finance"));
        s.setSubsidiaryReduceCash(rs.getDouble("subsidiary_reduce_cash"));
        s.setFinanceOutflowOther(rs.getDouble("finance_outflow_other"));
        s.setFinanceOutflowBalance(rs.getDouble("finance_outflow_balance"));
        s.setTotalFinanceOutflow(rs.getDouble("total_finance_outflow"));
        s.setFinanceNetcashOther(rs.getDouble("finance_netcash_other"));
        s.setFinanceNetcashBalance(rs.getDouble("finance_netcash_balance"));
        s.setNetcashFinance(rs.getDouble("netcash_finance"));
        s.setRateChangeEffect(rs.getDouble("rate_change_effect"));
        s.setCceAddOther(rs.getDouble("cce_add_other"));
        s.setCceAddBalance(rs.getDouble("cce_add_balance"));
        s.setCceAdd(rs.getDouble("cce_add"));
        s.setBeginCce(rs.getDouble("begin_cce"));
        s.setEndCceOther(rs.getDouble("end_cce_other"));
        s.setEndCceBalance(rs.getDouble("end_cce_balance"));
        s.setEndCce(rs.getDouble("end_cce"));
        s.setNetprofit(rs.getDouble("netprofit"));
        s.setAssetImpairment(rs.getDouble("asset_impairment"));
        s.setFaIrDepr(rs.getDouble("fa_ir_depr"));
        s.setOilgasBiologyDepr(rs.getDouble("oilgas_biology_depr"));
        s.setIrDepr(rs.getDouble("ir_depr"));
        s.setIaAmortize(rs.getDouble("ia_amortize"));
        s.setLpeAmortize(rs.getDouble("lpe_amortize"));
        s.setDeferIncomeAmortize(rs.getDouble("defer_income_amortize"));
        s.setPrepaidExpenseReduce(rs.getDouble("prepaid_expense_reduce"));
        s.setAccruedExpenseAdd(rs.getDouble("accrued_expense_add"));
        s.setDisposalLongassetLoss(rs.getDouble("disposal_longasset_loss"));
        s.setFaScrapLoss(rs.getDouble("fa_scrap_loss"));
        s.setFairvalueChangeLoss(rs.getDouble("fairvalue_change_loss"));
        s.setFinanceExpense(rs.getDouble("finance_expense"));
        s.setInvestLoss(rs.getDouble("invest_loss"));
        s.setDeferTax(rs.getDouble("defer_tax"));
        s.setDtAssetReduce(rs.getDouble("dt_asset_reduce"));
        s.setDtLiabAdd(rs.getDouble("dt_liab_add"));
        s.setPredictLiabAdd(rs.getDouble("predict_liab_add"));
        s.setInventoryReduce(rs.getDouble("inventory_reduce"));
        s.setOperateReceReduce(rs.getDouble("operate_rece_reduce"));
        s.setOperatePayableAdd(rs.getDouble("operate_payable_add"));
        s.setOther(rs.getDouble("other"));
        s.setOperateNetcashOthernote(rs.getDouble("operate_netcash_othernote"));
        s.setOperateNetcashBalancenote(rs.getDouble("operate_netcash_balancenote"));
        s.setNetcashOperatenote(rs.getDouble("netcash_operatenote"));
        s.setDebtTransferCapital(rs.getDouble("debt_transfer_capital"));
        s.setConvertBond1year(rs.getDouble("convert_bond_1year"));
        s.setFinleaseObtainFa(rs.getDouble("finlease_obtain_fa"));
        s.setUninvolveInvestfinOther(rs.getDouble("uninvolve_investfin_other"));
        s.setEndCash(rs.getDouble("end_cash"));
        s.setBeginCash(rs.getDouble("begin_cash"));
        s.setEndCashEquivalents(rs.getDouble("end_cash_equivalents"));
        s.setBeginCashEquivalents(rs.getDouble("begin_cash_equivalents"));
        s.setCceAddOthernote(rs.getDouble("cce_add_othernote"));
        s.setCceAddBalancenote(rs.getDouble("cce_add_balancenote"));
        s.setCceAddnote(rs.getDouble("cce_addnote"));
        s.setOpinionType(rs.getString("opinion_type"));
        s.setOsopinionType(rs.getString("osopinion_type"));
        s.setMinorityInterest(rs.getDouble("minority_interest"));
        s.setUserightAssetAmortize(rs.getDouble("useright_asset_amortize"));
        return s;
    }
}
