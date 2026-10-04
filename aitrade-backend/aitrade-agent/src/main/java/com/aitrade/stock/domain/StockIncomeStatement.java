package com.aitrade.stock.domain;

import lombok.Data;

import java.io.Serializable;

/*
CREATE TABLE "public"."income_statement" (
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
  "TOTAL_OPERATE_INCOME" numeric(20,2),
  "TOTAL_OPERATE_INCOME_YOY" numeric(10,4),
  "OPERATE_INCOME" numeric(20,2),
  "OPERATE_INCOME_YOY" numeric(10,4),
  "INTEREST_INCOME" numeric(20,2),
  "FEE_COMMISSION_INCOME" numeric(20,2),
  "OTHER_BUSINESS_INCOME" numeric(20,2),
  "TOTAL_OPERATE_COST" numeric(20,2),
  "TOTAL_OPERATE_COST_YOY" numeric(10,4),
  "OPERATE_COST" numeric(20,2),
  "OPERATE_COST_YOY" numeric(10,4),
  "INTEREST_EXPENSE" numeric(20,2),
  "FEE_COMMISSION_EXPENSE" numeric(20,2),
  "RESEARCH_EXPENSE" numeric(20,2),
  "RESEARCH_EXPENSE_YOY" numeric(10,4),
  "OPERATE_TAX_ADD" numeric(20,2),
  "OPERATE_TAX_ADD_YOY" numeric(10,4),
  "SALE_EXPENSE" numeric(20,2),
  "SALE_EXPENSE_YOY" numeric(10,4),
  "MANAGE_EXPENSE" numeric(20,2),
  "MANAGE_EXPENSE_YOY" numeric(10,4),
  "FINANCE_EXPENSE" numeric(20,2),
  "FINANCE_EXPENSE_YOY" numeric(10,4),
  "FE_INTEREST_EXPENSE" numeric(20,2),
  "FE_INTEREST_INCOME" numeric(20,2),
  "INVEST_INCOME" numeric(20,2),
  "INVEST_INCOME_YOY" numeric(10,4),
  "INVEST_JOINT_INCOME" numeric(20,2),
  "ASSET_DISPOSAL_INCOME" numeric(20,2),
  "ASSET_IMPAIRMENT_INCOME" numeric(20,2),
  "CREDIT_IMPAIRMENT_INCOME" numeric(20,2),
  "OTHER_INCOME" numeric(20,2),
  "FAIRVALUE_CHANGE_INCOME" numeric(20,2),
  "OPERATE_PROFIT" numeric(20,2),
  "OPERATE_PROFIT_YOY" numeric(10,4),
  "NONBUSINESS_INCOME" numeric(20,2),
  "NONBUSINESS_EXPENSE" numeric(20,2),
  "TOTAL_PROFIT" numeric(20,2),
  "TOTAL_PROFIT_YOY" numeric(10,4),
  "INCOME_TAX" numeric(20,2),
  "NETPROFIT" numeric(20,2),
  "NETPROFIT_YOY" numeric(10,4),
  "CONTINUED_NETPROFIT" numeric(20,2),
  "PARENT_NETPROFIT" numeric(20,2),
  "MINORITY_INTEREST" numeric(20,2),
  "DEDUCT_PARENT_NETPROFIT" numeric(20,2),
  "BASIC_EPS" numeric(10,4),
  "DILUTED_EPS" numeric(10,4),
  "OTHER_COMPRE_INCOME" numeric(20,2),
  "PARENT_OCI" numeric(20,2),
  "ABLE_OCI" numeric(20,2),
  "UNABLE_OCI" numeric(20,2),
  "CONVERT_DIFF" numeric(20,2),
  "TOTAL_COMPRE_INCOME" numeric(20,2),
  "PARENT_TCI" numeric(20,2),
  "MINORITY_TCI" numeric(20,2)
)
;

ALTER TABLE "public"."income_statement"
  OWNER TO "postgres";

COMMENT ON COLUMN "public"."income_statement"."SECUCODE" IS '证券代码';

COMMENT ON COLUMN "public"."income_statement"."REPORT_DATE" IS '报告日期';

COMMENT ON COLUMN "public"."income_statement"."REPORT_TYPE" IS '报告类型';

COMMENT ON COLUMN "public"."income_statement"."TOTAL_OPERATE_INCOME" IS '营业总收入';

COMMENT ON COLUMN "public"."income_statement"."OPERATE_INCOME" IS '营业收入';

COMMENT ON COLUMN "public"."income_statement"."INTEREST_INCOME" IS '利息收入';

COMMENT ON COLUMN "public"."income_statement"."FEE_COMMISSION_INCOME" IS '手续费及佣金收入';

COMMENT ON COLUMN "public"."income_statement"."OTHER_BUSINESS_INCOME" IS '其他业务收入';

COMMENT ON COLUMN "public"."income_statement"."TOTAL_OPERATE_COST" IS '营业总成本';

COMMENT ON COLUMN "public"."income_statement"."OPERATE_COST" IS '营业成本';

COMMENT ON COLUMN "public"."income_statement"."INTEREST_EXPENSE" IS '利息支出';

COMMENT ON COLUMN "public"."income_statement"."FEE_COMMISSION_EXPENSE" IS '手续费及佣金支出';

COMMENT ON COLUMN "public"."income_statement"."RESEARCH_EXPENSE" IS '研发费用';

COMMENT ON COLUMN "public"."income_statement"."OPERATE_TAX_ADD" IS '税金及附加';

COMMENT ON COLUMN "public"."income_statement"."SALE_EXPENSE" IS '销售费用';

COMMENT ON COLUMN "public"."income_statement"."MANAGE_EXPENSE" IS '管理费用';

COMMENT ON COLUMN "public"."income_statement"."FINANCE_EXPENSE" IS '财务费用';

COMMENT ON COLUMN "public"."income_statement"."FE_INTEREST_EXPENSE" IS '其中:利息费用';

COMMENT ON COLUMN "public"."income_statement"."FE_INTEREST_INCOME" IS '其中:利息收入';

COMMENT ON COLUMN "public"."income_statement"."INVEST_INCOME" IS '投资收益';

COMMENT ON COLUMN "public"."income_statement"."INVEST_JOINT_INCOME" IS '其中:对联营企业和合营企业的投资收益';

COMMENT ON COLUMN "public"."income_statement"."ASSET_DISPOSAL_INCOME" IS '资产处置收益';

COMMENT ON COLUMN "public"."income_statement"."ASSET_IMPAIRMENT_INCOME" IS '资产减值损失(新)';

COMMENT ON COLUMN "public"."income_statement"."CREDIT_IMPAIRMENT_INCOME" IS '信用减值损失(新)';

COMMENT ON COLUMN "public"."income_statement"."OTHER_INCOME" IS '其他收益';

COMMENT ON COLUMN "public"."income_statement"."FAIRVALUE_CHANGE_INCOME" IS '公允价值变动收益';

COMMENT ON COLUMN "public"."income_statement"."OPERATE_PROFIT" IS '营业利润';

COMMENT ON COLUMN "public"."income_statement"."NONBUSINESS_INCOME" IS '加:营业外收入';

COMMENT ON COLUMN "public"."income_statement"."NONBUSINESS_EXPENSE" IS '减:营业外支出';

COMMENT ON COLUMN "public"."income_statement"."TOTAL_PROFIT" IS '利润总额';

COMMENT ON COLUMN "public"."income_statement"."INCOME_TAX" IS '减:所得税费用';

COMMENT ON COLUMN "public"."income_statement"."NETPROFIT" IS '净利润';

COMMENT ON COLUMN "public"."income_statement"."CONTINUED_NETPROFIT" IS '持续经营净利润';

COMMENT ON COLUMN "public"."income_statement"."PARENT_NETPROFIT" IS '归属于母公司股东的净利润';

COMMENT ON COLUMN "public"."income_statement"."MINORITY_INTEREST" IS '少数股东损益';

COMMENT ON COLUMN "public"."income_statement"."DEDUCT_PARENT_NETPROFIT" IS '扣除非经常性损益后的净利润';

COMMENT ON COLUMN "public"."income_statement"."BASIC_EPS" IS '基本每股收益';

COMMENT ON COLUMN "public"."income_statement"."DILUTED_EPS" IS '稀释每股收益';

COMMENT ON COLUMN "public"."income_statement"."OTHER_COMPRE_INCOME" IS '其他综合收益';

COMMENT ON COLUMN "public"."income_statement"."PARENT_OCI" IS '归属于母公司股东的其他综合收益';

COMMENT ON COLUMN "public"."income_statement"."ABLE_OCI" IS '以后将重分类进损益的其他综合收益';

COMMENT ON COLUMN "public"."income_statement"."UNABLE_OCI" IS '不能重分类进损益的其他综合收益';

COMMENT ON COLUMN "public"."income_statement"."CONVERT_DIFF" IS '外币报表折算差额';

COMMENT ON COLUMN "public"."income_statement"."TOTAL_COMPRE_INCOME" IS '综合收益总额';

COMMENT ON COLUMN "public"."income_statement"."PARENT_TCI" IS '归属于母公司股东的综合收益总额';

COMMENT ON COLUMN "public"."income_statement"."MINORITY_TCI" IS '归属于少数股东的综合收益总额';

COMMENT ON TABLE "public"."income_statement" IS '利润表';
 */

@Data
public class StockIncomeStatement implements Serializable {
    private static final long serialVersionUID = 1L;
    private String secucode;// 证券代码
    private String securityCode;// 证券代码
    private String securityNameAbbr;// 证券名称
    private String orgCode;// 组织代码
    private String orgType;// 组织类型
    private String reportDate;// 报告日期
    private String reportType;// 报告类型
    private String reportDateName;// 报告日期名称
    private String securityTypeCode;// 证券类型代码
    private String noticeDate;// 公告日期
    private String updateDate;// 更新日期
    private String currency;// 币种
    private Double totalOperateIncome;// 营业总收入
    private Double totalOperateIncomeYoY;// 营业总收入同比增长率
    private Double operateIncome;// 营业收入
    private Double operateIncomeYoY;// 营业收入同比增长率
    private Double interestIncome;// 利息收入
    private Double feeCommissionIncome;// 手续费及佣金收入
    private Double otherBusinessIncome;// 其他业务收入
    private Double totalOperateCost;// 营业总成本
    private Double totalOperateCostYoY;// 营业总成本同比增长率
    private Double operateCost;// 营业成本
    private Double operateCostYoY;// 营业成本同比增长率
    private Double interestExpense;// 利息支出
    private Double feeCommissionExpense;// 手续费及佣金支出
    private Double researchExpense;// 研发费用
    private Double researchExpenseYoY;// 研发费用同比增长率
    private Double operateTaxAdd;// 税金及附加
    private Double operateTaxAddYoY;// 税金及附加同比增长率
    private Double saleExpense;// 销售费用
    private Double saleExpenseYoY;// 销售费用同比增长率
    private Double manageExpense;// 管理费用
    private Double manageExpenseYoY;// 管理费用同比增长率
    private Double financeExpense;// 财务费用
    private Double financeExpenseYoY;// 财务费用同比增长率
    private Double feInterestExpense;// 其中:利息费用
    private Double feInterestIncome;// 其中:利息收入
    private Double investIncome;// 投资收益
    private Double investIncomeYoY;// 投资收益同比增长率
    private Double investJointIncome;// 对联营企业和合营企业的投资收益
    private Double assetDisposalIncome;// 资产处置收益
    private Double assetImpairmentIncome;// 资产减值损失(新)
    private Double creditImpairmentIncome;// 信用减值损失(新)
    private Double otherIncome;// 其他收益
    private Double fairvalueChangeIncome;// 公允价值变动收益
    private Double operateProfit;// 营业利润
    private Double operateProfitYoY;// 营业利润同比增长率
    private Double nonbusinessIncome;// 加:营业外收入
    private Double nonbusinessExpense;// 减:营业外支出
    private Double totalProfit;// 利润总额
    private Double totalProfitYoY;// 利润总额同比增长率
    private Double incomeTax;// 所得税费用
    private Double netprofit;// 净利润
    private Double netprofitYoY;// 净利润同比增长率
    private Double continuedNetprofit;// 持续经营净利润
    private Double parentNetprofit;// 归属于母公司股东的净利润
    private Double minorityInterest;// 少数股东损益
    private Double deductParentNetprofit;// 扣除非经常性损益后的净利润
    private Double basicEps;// 基本每股收益
    private Double dilutedEps;// 稀释每股收益
    private Double otherCompreIncome;// 其他综合收益
    private Double parentOci;// 归属于母公司股东的其他综合收益
    private Double ableOci;// 以后将重分类进损益的其他综合收益
    private Double unableOci;// 不能重分类进损益的其他综合收益
    private Double convertDiff;// 外币报表折算差额
    private Double totalCompreIncome;// 综合收益总额
    private Double parentTci;// 归属于母公司股东的综合收益总额
    private Double minorityTci;// 归属于少数股东的综合收益总额
}
