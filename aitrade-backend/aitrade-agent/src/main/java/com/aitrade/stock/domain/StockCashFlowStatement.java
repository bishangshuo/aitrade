package com.aitrade.stock.domain;

import lombok.Data;

import java.io.Serializable;

/*
CREATE TABLE "public"."cash_flow_statement" (
  "SECUCODE" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "SECURITY_CODE" varchar(10) COLLATE "pg_catalog"."default",
  "SECURITY_NAME_ABBR" varchar(50) COLLATE "pg_catalog"."default",
  "ORG_CODE" varchar(20) COLLATE "pg_catalog"."default",
  "ORG_TYPE" varchar(20) COLLATE "pg_catalog"."default",
  "REPORT_DATE" timestamptz(6) NOT NULL,
  "REPORT_TYPE" varchar(10) COLLATE "pg_catalog"."default",
  "REPORT_DATE_NAME" varchar(20) COLLATE "pg_catalog"."default",
  "SECURITY_TYPE_CODE" varchar(20) COLLATE "pg_catalog"."default",
  "NOTICE_DATE" timestamptz(6),
  "UPDATE_DATE" timestamptz(6),
  "CURRENCY" varchar(5) COLLATE "pg_catalog"."default",
  "SALES_SERVICES" numeric(20,2),
  "DEPOSIT_INTERBANK_ADD" numeric(20,2),
  "LOAN_PBC_ADD" numeric(20,2),
  "OFI_BF_ADD" numeric(20,2),
  "RECEIVE_ORIGIC_PREMIUM" numeric(20,2),
  "RECEIVE_REINSURE_NET" numeric(20,2),
  "INSURED_INVEST_ADD" numeric(20,2),
  "DISPOSAL_TFA_ADD" numeric(20,2),
  "RECEIVE_INTEREST_COMMISSION" numeric(20,2),
  "BORROW_FUND_ADD" numeric(20,2),
  "LOAN_ADVANCE_REDUCE" numeric(20,2),
  "REPO_BUSINESS_ADD" numeric(20,2),
  "RECEIVE_TAX_REFUND" numeric(20,2),
  "RECEIVE_OTHER_OPERATE" numeric(20,2),
  "OPERATE_INFLOW_OTHER" numeric(20,2),
  "OPERATE_INFLOW_BALANCE" numeric(20,2),
  "TOTAL_OPERATE_INFLOW" numeric(20,2),
  "BUY_SERVICES" numeric(20,2),
  "LOAN_ADVANCE_ADD" numeric(20,2),
  "PBC_INTERBANK_ADD" numeric(20,2),
  "PAY_ORIGIC_COMPENSATE" numeric(20,2),
  "PAY_INTEREST_COMMISSION" numeric(20,2),
  "PAY_POLICY_BONUS" numeric(20,2),
  "PAY_STAFF_CASH" numeric(20,2),
  "PAY_ALL_TAX" numeric(20,2),
  "PAY_OTHER_OPERATE" numeric(20,2),
  "OPERATE_OUTFLOW_OTHER" numeric(20,2),
  "OPERATE_OUTFLOW_BALANCE" numeric(20,2),
  "TOTAL_OPERATE_OUTFLOW" numeric(20,2),
  "OPERATE_NETCASH_OTHER" numeric(20,2),
  "OPERATE_NETCASH_BALANCE" numeric(20,2),
  "NETCASH_OPERATE" numeric(20,2),
  "WITHDRAW_INVEST" numeric(20,2),
  "RECEIVE_INVEST_INCOME" numeric(20,2),
  "DISPOSAL_LONG_ASSET" numeric(20,2),
  "DISPOSAL_SUBSIDIARY_OTHER" numeric(20,2),
  "REDUCE_PLEDGE_TIMEDEPOSITS" numeric(20,2),
  "RECEIVE_OTHER_INVEST" numeric(20,2),
  "INVEST_INFLOW_OTHER" numeric(20,2),
  "INVEST_INFLOW_BALANCE" numeric(20,2),
  "TOTAL_INVEST_INFLOW" numeric(20,2),
  "CONSTRUCT_LONG_ASSET" numeric(20,2),
  "INVEST_PAY_CASH" numeric(20,2),
  "PLEDGE_LOAN_ADD" numeric(20,2),
  "OBTAIN_SUBSIDIARY_OTHER" numeric(20,2),
  "ADD_PLEDGE_TIMEDEPOSITS" numeric(20,2),
  "PAY_OTHER_INVEST" numeric(20,2),
  "INVEST_OUTFLOW_OTHER" numeric(20,2),
  "INVEST_OUTFLOW_BALANCE" numeric(20,2),
  "TOTAL_INVEST_OUTFLOW" numeric(20,2),
  "INVEST_NETCASH_OTHER" numeric(20,2),
  "INVEST_NETCASH_BALANCE" numeric(20,2),
  "NETCASH_INVEST" numeric(20,2),
  "ACCEPT_INVEST_CASH" numeric(20,2),
  "SUBSIDIARY_ACCEPT_INVEST" numeric(20,2),
  "RECEIVE_LOAN_CASH" numeric(20,2),
  "ISSUE_BOND" numeric(20,2),
  "RECEIVE_OTHER_FINANCE" numeric(20,2),
  "FINANCE_INFLOW_OTHER" numeric(20,2),
  "FINANCE_INFLOW_BALANCE" numeric(20,2),
  "TOTAL_FINANCE_INFLOW" numeric(20,2),
  "PAY_DEBT_CASH" numeric(20,2),
  "ASSIGN_DIVIDEND_PORFIT" numeric(20,2),
  "SUBSIDIARY_PAY_DIVIDEND" numeric(20,2),
  "BUY_SUBSIDIARY_EQUITY" numeric(20,2),
  "PAY_OTHER_FINANCE" numeric(20,2),
  "SUBSIDIARY_REDUCE_CASH" numeric(20,2),
  "FINANCE_OUTFLOW_OTHER" numeric(20,2),
  "FINANCE_OUTFLOW_BALANCE" numeric(20,2),
  "TOTAL_FINANCE_OUTFLOW" numeric(20,2),
  "FINANCE_NETCASH_OTHER" numeric(20,2),
  "FINANCE_NETCASH_BALANCE" numeric(20,2),
  "NETCASH_FINANCE" numeric(20,2),
  "RATE_CHANGE_EFFECT" numeric(20,2),
  "CCE_ADD_OTHER" numeric(20,2),
  "CCE_ADD_BALANCE" numeric(20,2),
  "CCE_ADD" numeric(20,2),
  "BEGIN_CCE" numeric(20,2),
  "END_CCE_OTHER" numeric(20,2),
  "END_CCE_BALANCE" numeric(20,2),
  "END_CCE" numeric(20,2),
  "NETPROFIT" numeric(20,2),
  "ASSET_IMPAIRMENT" numeric(20,2),
  "FA_IR_DEPR" numeric(20,2),
  "OILGAS_BIOLOGY_DEPR" numeric(20,2),
  "IR_DEPR" numeric(20,2),
  "IA_AMORTIZE" numeric(20,2),
  "LPE_AMORTIZE" numeric(20,2),
  "DEFER_INCOME_AMORTIZE" numeric(20,2),
  "PREPAID_EXPENSE_REDUCE" numeric(20,2),
  "ACCRUED_EXPENSE_ADD" numeric(20,2),
  "DISPOSAL_LONGASSET_LOSS" numeric(20,2),
  "FA_SCRAP_LOSS" numeric(20,2),
  "FAIRVALUE_CHANGE_LOSS" numeric(20,2),
  "FINANCE_EXPENSE" numeric(20,2),
  "INVEST_LOSS" numeric(20,2),
  "DEFER_TAX" numeric(20,2),
  "DT_ASSET_REDUCE" numeric(20,2),
  "DT_LIAB_ADD" numeric(20,2),
  "PREDICT_LIAB_ADD" numeric(20,2),
  "INVENTORY_REDUCE" numeric(20,2),
  "OPERATE_RECE_REDUCE" numeric(20,2),
  "OPERATE_PAYABLE_ADD" numeric(20,2),
  "OTHER" numeric(20,2),
  "OPERATE_NETCASH_OTHERNOTE" numeric(20,2),
  "OPERATE_NETCASH_BALANCENOTE" numeric(20,2),
  "NETCASH_OPERATENOTE" numeric(20,2),
  "DEBT_TRANSFER_CAPITAL" numeric(20,2),
  "CONVERT_BOND_1YEAR" numeric(20,2),
  "FINLEASE_OBTAIN_FA" numeric(20,2),
  "UNINVOLVE_INVESTFIN_OTHER" numeric(20,2),
  "END_CASH" numeric(20,2),
  "BEGIN_CASH" numeric(20,2),
  "END_CASH_EQUIVALENTS" numeric(20,2),
  "BEGIN_CASH_EQUIVALENTS" numeric(20,2),
  "CCE_ADD_OTHERNOTE" numeric(20,2),
  "CCE_ADD_BALANCENOTE" numeric(20,2),
  "CCE_ADDNOTE" numeric(20,2),
  "OPINION_TYPE" varchar(20) COLLATE "pg_catalog"."default",
  "OSOPINION_TYPE" varchar(20) COLLATE "pg_catalog"."default",
  "MINORITY_INTEREST" numeric(20,2),
  "USERIGHT_ASSET_AMORTIZE" numeric(20,2)
)
;

ALTER TABLE "public"."cash_flow_statement"
  OWNER TO "postgres";

COMMENT ON COLUMN "public"."cash_flow_statement"."SECUCODE" IS '证券代码（含市场后缀）';

COMMENT ON COLUMN "public"."cash_flow_statement"."SECURITY_CODE" IS '证券代码（纯数字）';

COMMENT ON COLUMN "public"."cash_flow_statement"."SECURITY_NAME_ABBR" IS '证券简称';

COMMENT ON COLUMN "public"."cash_flow_statement"."ORG_CODE" IS '机构代码';

COMMENT ON COLUMN "public"."cash_flow_statement"."ORG_TYPE" IS '机构类型';

COMMENT ON COLUMN "public"."cash_flow_statement"."REPORT_DATE" IS '报告日期';

COMMENT ON COLUMN "public"."cash_flow_statement"."REPORT_TYPE" IS '报告类型（如一季报、中报等）';

COMMENT ON COLUMN "public"."cash_flow_statement"."REPORT_DATE_NAME" IS '报告期名称';

COMMENT ON COLUMN "public"."cash_flow_statement"."SECURITY_TYPE_CODE" IS '证券类型代码';

COMMENT ON COLUMN "public"."cash_flow_statement"."NOTICE_DATE" IS '公告日期';

COMMENT ON COLUMN "public"."cash_flow_statement"."UPDATE_DATE" IS '数据更新日期';

COMMENT ON COLUMN "public"."cash_flow_statement"."CURRENCY" IS '货币代码';

COMMENT ON COLUMN "public"."cash_flow_statement"."SALES_SERVICES" IS '销售商品、提供劳务收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."DEPOSIT_INTERBANK_ADD" IS '客户存款和同业存放款项净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."LOAN_PBC_ADD" IS '向中央银行借款净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."OFI_BF_ADD" IS '向其他金融机构拆入资金净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_ORIGIC_PREMIUM" IS '收到原保险合同保费取得的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_REINSURE_NET" IS '收到再保业务现金净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."INSURED_INVEST_ADD" IS '保户储金及投资款净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."DISPOSAL_TFA_ADD" IS '处置交易性金融资产净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_INTEREST_COMMISSION" IS '收取利息、手续费及佣金的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."BORROW_FUND_ADD" IS '拆入资金净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."LOAN_ADVANCE_REDUCE" IS '贷款及垫款净减少额（或净增加额的负数）';

COMMENT ON COLUMN "public"."cash_flow_statement"."REPO_BUSINESS_ADD" IS '回购业务资金净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_TAX_REFUND" IS '收到的税费返还';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_OTHER_OPERATE" IS '收到其他与经营活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_INFLOW_OTHER" IS '经营活动现金流入其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_INFLOW_BALANCE" IS '经营活动现金流入平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_OPERATE_INFLOW" IS '经营活动现金流入小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."BUY_SERVICES" IS '购买商品、接受劳务支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."LOAN_ADVANCE_ADD" IS '客户贷款及垫款净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."PBC_INTERBANK_ADD" IS '存放中央银行和同业款项净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_ORIGIC_COMPENSATE" IS '支付原保险合同赔付款项的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_INTEREST_COMMISSION" IS '支付利息、手续费及佣金的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_POLICY_BONUS" IS '支付保单红利的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_STAFF_CASH" IS '支付给职工以及为职工支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_ALL_TAX" IS '支付的各项税费';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_OTHER_OPERATE" IS '支付其他与经营活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_OUTFLOW_OTHER" IS '经营活动现金流出其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_OUTFLOW_BALANCE" IS '经营活动现金流出平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_OPERATE_OUTFLOW" IS '经营活动现金流出小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_NETCASH_OTHER" IS '经营活动净现金流其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_NETCASH_BALANCE" IS '经营活动净现金流平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."NETCASH_OPERATE" IS '经营活动产生的现金流量净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."WITHDRAW_INVEST" IS '收回投资收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_INVEST_INCOME" IS '取得投资收益收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."DISPOSAL_LONG_ASSET" IS '处置固定资产、无形资产和其他长期资产收回的现金净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."DISPOSAL_SUBSIDIARY_OTHER" IS '处置子公司及其他营业单位收到的现金净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."REDUCE_PLEDGE_TIMEDEPOSITS" IS '减少质押和定期存款所收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_OTHER_INVEST" IS '收到其他与投资活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_INFLOW_OTHER" IS '投资活动现金流入其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_INFLOW_BALANCE" IS '投资活动现金流入平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_INVEST_INFLOW" IS '投资活动现金流入小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."CONSTRUCT_LONG_ASSET" IS '购建固定资产、无形资产和其他长期资产支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_PAY_CASH" IS '投资支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PLEDGE_LOAN_ADD" IS '质押贷款净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."OBTAIN_SUBSIDIARY_OTHER" IS '取得子公司及其他营业单位支付的现金净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."ADD_PLEDGE_TIMEDEPOSITS" IS '增加质押和定期存款所支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_OTHER_INVEST" IS '支付其他与投资活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_OUTFLOW_OTHER" IS '投资活动现金流出其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_OUTFLOW_BALANCE" IS '投资活动现金流出平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_INVEST_OUTFLOW" IS '投资活动现金流出小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_NETCASH_OTHER" IS '投资活动净现金流其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_NETCASH_BALANCE" IS '投资活动净现金流平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."NETCASH_INVEST" IS '投资活动产生的现金流量净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."ACCEPT_INVEST_CASH" IS '吸收投资收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."SUBSIDIARY_ACCEPT_INVEST" IS '其中:子公司吸收少数股东投资收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_LOAN_CASH" IS '取得借款收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."ISSUE_BOND" IS '发行债券收到的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."RECEIVE_OTHER_FINANCE" IS '收到其他与筹资活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_INFLOW_OTHER" IS '筹资活动现金流入其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_INFLOW_BALANCE" IS '筹资活动现金流入平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_FINANCE_INFLOW" IS '筹资活动现金流入小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_DEBT_CASH" IS '偿还债务支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."ASSIGN_DIVIDEND_PORFIT" IS '分配股利、利润或偿付利息支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."SUBSIDIARY_PAY_DIVIDEND" IS '其中:子公司支付给少数股东的股利、利润';

COMMENT ON COLUMN "public"."cash_flow_statement"."BUY_SUBSIDIARY_EQUITY" IS '购买子公司少数股权支付的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."PAY_OTHER_FINANCE" IS '支付其他与筹资活动有关的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."SUBSIDIARY_REDUCE_CASH" IS '子公司减资支付给少数股东的现金';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_OUTFLOW_OTHER" IS '筹资活动现金流出其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_OUTFLOW_BALANCE" IS '筹资活动现金流出平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."TOTAL_FINANCE_OUTFLOW" IS '筹资活动现金流出小计';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_NETCASH_OTHER" IS '筹资活动净现金流其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_NETCASH_BALANCE" IS '筹资活动净现金流平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."NETCASH_FINANCE" IS '筹资活动产生的现金流量净额';

COMMENT ON COLUMN "public"."cash_flow_statement"."RATE_CHANGE_EFFECT" IS '汇率变动对现金及现金等价物的影响';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADD_OTHER" IS '现金及现金等价物净增加额其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADD_BALANCE" IS '现金及现金等价物净增加额平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADD" IS '现金及现金等价物净增加额';

COMMENT ON COLUMN "public"."cash_flow_statement"."BEGIN_CCE" IS '期初现金及现金等价物余额';

COMMENT ON COLUMN "public"."cash_flow_statement"."END_CCE_OTHER" IS '期末现金及现金等价物余额其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."END_CCE_BALANCE" IS '期末现金及现金等价物余额平衡项（通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."END_CCE" IS '期末现金及现金等价物余额';

COMMENT ON COLUMN "public"."cash_flow_statement"."NETPROFIT" IS '净利润（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."ASSET_IMPAIRMENT" IS '资产减值准备（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."FA_IR_DEPR" IS '固定资产折旧、油气资产折耗、生产性生物资产折旧（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OILGAS_BIOLOGY_DEPR" IS '油气资产折耗（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."IR_DEPR" IS '投资性房地产折旧（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."IA_AMORTIZE" IS '无形资产摊销（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."LPE_AMORTIZE" IS '长期待摊费用摊销（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DEFER_INCOME_AMORTIZE" IS '递延收益摊销（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."PREPAID_EXPENSE_REDUCE" IS '预付款项减少（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."ACCRUED_EXPENSE_ADD" IS '预提费用增加（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DISPOSAL_LONGASSET_LOSS" IS '处置固定资产、无形资产和其他长期资产的损失（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."FA_SCRAP_LOSS" IS '固定资产报废损失（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."FAIRVALUE_CHANGE_LOSS" IS '公允价值变动损失（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINANCE_EXPENSE" IS '财务费用（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVEST_LOSS" IS '投资损失（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DEFER_TAX" IS '递延所得税（补充资料，减少以负数填列）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DT_ASSET_REDUCE" IS '递延所得税资产减少（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DT_LIAB_ADD" IS '递延所得税负债增加（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."PREDICT_LIAB_ADD" IS '预计负债增加（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."INVENTORY_REDUCE" IS '存货的减少（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_RECE_REDUCE" IS '经营性应收项目的减少（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_PAYABLE_ADD" IS '经营性应付项目的增加（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OTHER" IS '其他（补充资料）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_NETCASH_OTHERNOTE" IS '经营活动净现金流其他项（补充资料校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPERATE_NETCASH_BALANCENOTE" IS '经营活动净现金流平衡项（补充资料校验，通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."NETCASH_OPERATENOTE" IS '经营活动产生的现金流量净额（补充资料校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."DEBT_TRANSFER_CAPITAL" IS '债务转为资本（不涉及现金）';

COMMENT ON COLUMN "public"."cash_flow_statement"."CONVERT_BOND_1YEAR" IS '一年内到期的可转换公司债券（不涉及现金）';

COMMENT ON COLUMN "public"."cash_flow_statement"."FINLEASE_OBTAIN_FA" IS '融资租入固定资产（不涉及现金）';

COMMENT ON COLUMN "public"."cash_flow_statement"."UNINVOLVE_INVESTFIN_OTHER" IS '不涉及现金收支的重大投资和筹资活动其他项';

COMMENT ON COLUMN "public"."cash_flow_statement"."END_CASH" IS '现金的期末余额（校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."BEGIN_CASH" IS '现金的期初余额（校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."END_CASH_EQUIVALENTS" IS '现金等价物的期末余额（校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."BEGIN_CASH_EQUIVALENTS" IS '现金等价物的期初余额（校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADD_OTHERNOTE" IS '现金及现金等价物净增加额其他项（校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADD_BALANCENOTE" IS '现金及现金等价物净增加额平衡项（校验，通常为0）';

COMMENT ON COLUMN "public"."cash_flow_statement"."CCE_ADDNOTE" IS '现金及现金等价物净增加额（补充资料校验）';

COMMENT ON COLUMN "public"."cash_flow_statement"."OPINION_TYPE" IS '审计意见类型';

COMMENT ON COLUMN "public"."cash_flow_statement"."OSOPINION_TYPE" IS '审计意见类型（其他）';

COMMENT ON COLUMN "public"."cash_flow_statement"."MINORITY_INTEREST" IS '少数股东权益（或损益）';

COMMENT ON COLUMN "public"."cash_flow_statement"."USERIGHT_ASSET_AMORTIZE" IS '使用权资产折旧（摊销）';

COMMENT ON TABLE "public"."cash_flow_statement" IS '现金流量表（字段与JSON完全一致）';
 */

// ... existing code ...
@Data
public class StockCashFlowStatement implements Serializable {
    private static final long serialVersionUID = 1L;
    private String secucode;// 证券代码（含市场后缀）
    private String securityCode;// 证券代码（纯数字）
    private String securityNameAbbr;// 证券简称
    private String orgCode;// 机构代码
    private String orgType;// 机构类型
    private String reportDate;// 报告日期
    private String reportType;// 报告类型（如一季报、中报等）
    private String reportDateName;// 报告期名称
    private String securityTypeCode;// 证券类型代码
    private String noticeDate;// 公告日期
    private String updateDate;// 数据更新日期
    private String currency;// 货币代码
    private Double salesServices;// 销售商品、提供劳务收到的现金
    private Double depositInterbankAdd;// 客户存款和同业存放款项净增加额
    private Double loanPbcAdd;// 向中央银行借款净增加额
    private Double ofiBfAdd;// 向其他金融机构拆入资金净增加额
    private Double receiveOrigicPremium;// 收到原保险合同保费取得的现金
    private Double receiveReinsureNet;// 收到再保业务现金净额
    private Double insuredInvestAdd;// 保户储金及投资款净增加额
    private Double disposalTfaAdd;// 处置交易性金融资产净增加额
    private Double receiveInterestCommission;// 收取利息、手续费及佣金的现金
    private Double borrowFundAdd;// 拆入资金净增加额
    private Double loanAdvanceReduce;// 贷款及垫款净减少额（或净增加额的负数）
    private Double repoBusinessAdd;// 回购业务资金净增加额
    private Double receiveTaxRefund;// 收到的税费返还
    private Double receiveOtherOperate;// 收到其他与经营活动有关的现金
    private Double operateInflowOther;// 经营活动现金流入其他项
    private Double operateInflowBalance;// 经营活动现金流入平衡项（通常为0）
    private Double totalOperateInflow;// 经营活动现金流入小计
    private Double buyServices;// 购买商品、接受劳务支付的现金
    private Double loanAdvanceAdd;// 客户贷款及垫款净增加额
    private Double pbcInterbankAdd;// 存放中央银行和同业款项净增加额
    private Double payOrigicCompensate;// 支付原保险合同赔付款项的现金
    private Double payInterestCommission;// 支付利息、手续费及佣金的现金
    private Double payPolicyBonus;// 支付保单红利的现金
    private Double payStaffCash;// 支付给职工以及为职工支付的现金
    private Double payAllTax;// 支付的各项税费
    private Double payOtherOperate;// 支付其他与经营活动有关的现金
    private Double operateOutflowOther;// 经营活动现金流出其他项
    private Double operateOutflowBalance;// 经营活动现金流出平衡项（通常为0）
    private Double totalOperateOutflow;// 经营活动现金流出小计
    private Double operateNetcashOther;// 经营活动净现金流其他项
    private Double operateNetcashBalance;// 经营活动净现金流平衡项（通常为0）
    private Double netcashOperate;// 经营活动产生的现金流量净额
    private Double withdrawInvest;// 收回投资收到的现金
    private Double receiveInvestIncome;// 取得投资收益收到的现金
    private Double disposalLongAsset;// 处置固定资产、无形资产和其他长期资产收回的现金净额
    private Double disposalSubsidiaryOther;// 处置子公司及其他营业单位收到的现金净额
    private Double reducePledgeTimedeposits;// 减少质押和定期存款所收到的现金
    private Double receiveOtherInvest;// 收到其他与投资活动有关的现金
    private Double investInflowOther;// 投资活动现金流入其他项
    private Double investInflowBalance;// 投资活动现金流入平衡项（通常为0）
    private Double totalInvestInflow;// 投资活动现金流入小计
    private Double constructLongAsset;// 购建固定资产、无形资产和其他长期资产支付的现金
    private Double investPayCash;// 投资支付的现金
    private Double pledgeLoanAdd;// 质押贷款净增加额
    private Double obtainSubsidiaryOther;// 取得子公司及其他营业单位支付的现金净额
    private Double addPledgeTimedeposits;// 增加质押和定期存款所支付的现金
    private Double payOtherInvest;// 支付其他与投资活动有关的现金
    private Double investOutflowOther;// 投资活动现金流出其他项
    private Double investOutflowBalance;// 投资活动现金流出平衡项（通常为0）
    private Double totalInvestOutflow;// 投资活动现金流出小计
    private Double investNetcashOther;// 投资活动净现金流其他项
    private Double investNetcashBalance;// 投资活动净现金流平衡项（通常为0）
    private Double netcashInvest;// 投资活动产生的现金流量净额
    private Double acceptInvestCash;// 吸收投资收到的现金
    private Double subsidiaryAcceptInvest;// 其中:子公司吸收少数股东投资收到的现金
    private Double receiveLoanCash;// 取得借款收到的现金
    private Double issueBond;// 发行债券收到的现金
    private Double receiveOtherFinance;// 收到其他与筹资活动有关的现金
    private Double financeInflowOther;// 筹资活动现金流入其他项
    private Double financeInflowBalance;// 筹资活动现金流入平衡项（通常为0）
    private Double totalFinanceInflow;// 筹资活动现金流入小计
    private Double payDebtCash;// 偿还债务支付的现金
    private Double assignDividendPorfit;// 分配股利、利润或偿付利息支付的现金
    private Double subsidiaryPayDividend;// 其中:子公司支付给少数股东的股利、利润
    private Double buySubsidiaryEquity;// 购买子公司少数股权支付的现金
    private Double payOtherFinance;// 支付其他与筹资活动有关的现金
    private Double subsidiaryReduceCash;// 子公司减资支付给少数股东的现金
    private Double financeOutflowOther;// 筹资活动现金流出其他项
    private Double financeOutflowBalance;// 筹资活动现金流出平衡项（通常为0）
    private Double totalFinanceOutflow;// 筹资活动现金流出小计
    private Double financeNetcashOther;// 筹资活动净现金流其他项
    private Double financeNetcashBalance;// 筹资活动净现金流平衡项（通常为0）
    private Double netcashFinance;// 筹资活动产生的现金流量净额
    private Double rateChangeEffect;// 汇率变动对现金及现金等价物的影响
    private Double cceAddOther;// 现金及现金等价物净增加额其他项
    private Double cceAddBalance;// 现金及现金等价物净增加额平衡项（通常为0）
    private Double cceAdd;// 现金及现金等价物净增加额
    private Double beginCce;// 期初现金及现金等价物余额
    private Double endCceOther;// 期末现金及现金等价物余额其他项
    private Double endCceBalance;// 期末现金及现金等价物余额平衡项（通常为0）
    private Double endCce;// 期末现金及现金等价物余额
    private Double netprofit;// 净利润（补充资料）
    private Double assetImpairment;// 资产减值准备（补充资料）
    private Double faIrDepr;// 固定资产折旧、油气资产折耗、生产性生物资产折旧（补充资料）
    private Double oilgasBiologyDepr;// 油气资产折耗（补充资料）
    private Double irDepr;// 投资性房地产折旧（补充资料）
    private Double iaAmortize;// 无形资产摊销（补充资料）
    private Double lpeAmortize;// 长期待摊费用摊销（补充资料）
    private Double deferIncomeAmortize;// 递延收益摊销（补充资料）
    private Double prepaidExpenseReduce;// 预付款项减少（补充资料）
    private Double accruedExpenseAdd;// 预提费用增加（补充资料）
    private Double disposalLongassetLoss;// 处置固定资产、无形资产和其他长期资产的损失（补充资料）
    private Double faScrapLoss;// 固定资产报废损失（补充资料）
    private Double fairvalueChangeLoss;// 公允价值变动损失（补充资料）
    private Double financeExpense;// 财务费用（补充资料）
    private Double investLoss;// 投资损失（补充资料）
    private Double deferTax;// 递延所得税（补充资料，减少以负数填列）
    private Double dtAssetReduce;// 递延所得税资产减少（补充资料）
    private Double dtLiabAdd;// 递延所得税负债增加（补充资料）
    private Double predictLiabAdd;// 预计负债增加（补充资料）
    private Double inventoryReduce;// 存货的减少（补充资料）
    private Double operateReceReduce;// 经营性应收项目的减少（补充资料）
    private Double operatePayableAdd;// 经营性应付项目的增加（补充资料）
    private Double other;// 其他（补充资料）
    private Double operateNetcashOthernote;// 经营活动净现金流其他项（补充资料校验）
    private Double operateNetcashBalancenote;// 经营活动净现金流平衡项（补充资料校验，通常为0）
    private Double netcashOperatenote;// 经营活动产生的现金流量净额（补充资料校验）
    private Double debtTransferCapital;// 债务转为资本（不涉及现金）
    private Double convertBond1year;// 一年内到期的可转换公司债券（不涉及现金）
    private Double finleaseObtainFa;// 融资租入固定资产（不涉及现金）
    private Double uninvolveInvestfinOther;// 不涉及现金收支的重大投资和筹资活动其他项
    private Double endCash;// 现金的期末余额（校验）
    private Double beginCash;// 现金的期初余额（校验）
    private Double endCashEquivalents;// 现金等价物的期末余额（校验）
    private Double beginCashEquivalents;// 现金等价物的期初余额（校验）
    private Double cceAddOthernote;// 现金及现金等价物净增加额其他项（校验）
    private Double cceAddBalancenote;// 现金及现金等价物净增加额平衡项（校验，通常为0）
    private Double cceAddnote;// 现金及现金等价物净增加额（补充资料校验）
    private String opinionType;// 审计意见类型
    private String osopinionType;// 审计意见类型（其他）
    private Double minorityInterest;// 少数股东权益（或损益）
    private Double userightAssetAmortize;// 使用权资产折旧（摊销）

}
