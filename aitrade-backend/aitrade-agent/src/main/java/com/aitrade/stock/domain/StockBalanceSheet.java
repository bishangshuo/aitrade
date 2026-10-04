package com.aitrade.stock.domain;

import lombok.Data;

import java.io.Serializable;

/*
CREATE TABLE "public"."balance_sheet" (
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
  "MONETARYFUNDS" numeric(20,2),
  "NOTE_ACCOUNTS_RECE" numeric(20,2),
  "NOTE_RECE" numeric(20,2),
  "ACCOUNTS_RECE" numeric(20,2),
  "PREPAYMENT" numeric(20,2),
  "OTHER_RECE" numeric(20,2),
  "INVENTORY" numeric(20,2),
  "OTHER_CURRENT_ASSET" numeric(20,2),
  "TOTAL_CURRENT_ASSETS" numeric(20,2),
  "LONG_EQUITY_INVEST" numeric(20,2),
  "OTHER_EQUITY_INVEST" numeric(20,2),
  "OTHER_NONCURRENT_FINASSET" numeric(20,2),
  "INVEST_REALESTATE" numeric(20,2),
  "FIXED_ASSET" numeric(20,2),
  "CIP" numeric(20,2),
  "USERIGHT_ASSET" numeric(20,2),
  "INTANGIBLE_ASSET" numeric(20,2),
  "LONG_PREPAID_EXPENSE" numeric(20,2),
  "DEFER_TAX_ASSET" numeric(20,2),
  "OTHER_NONCURRENT_ASSET" numeric(20,2),
  "TOTAL_NONCURRENT_ASSETS" numeric(20,2),
  "TOTAL_ASSETS" numeric(20,2),
  "SHORT_LOAN" numeric(20,2),
  "NOTE_ACCOUNTS_PAYABLE" numeric(20,2),
  "NOTE_PAYABLE" numeric(20,2),
  "ACCOUNTS_PAYABLE" numeric(20,2),
  "ADVANCE_RECEIVABLES" numeric(20,2),
  "CONTRACT_LIAB" numeric(20,2),
  "STAFF_SALARY_PAYABLE" numeric(20,2),
  "TAX_PAYABLE" numeric(20,2),
  "OTHER_PAYABLE" numeric(20,2),
  "DIVIDEND_PAYABLE" numeric(20,2),
  "NONCURRENT_LIAB_1YEAR" numeric(20,2),
  "OTHER_CURRENT_LIAB" numeric(20,2),
  "TOTAL_CURRENT_LIAB" numeric(20,2),
  "LONG_LOAN" numeric(20,2),
  "LEASE_LIAB" numeric(20,2),
  "LONG_PAYABLE" numeric(20,2),
  "DEFER_TAX_LIAB" numeric(20,2),
  "OTHER_NONCURRENT_LIAB" numeric(20,2),
  "TOTAL_NONCURRENT_LIAB" numeric(20,2),
  "TOTAL_LIABILITIES" numeric(20,2),
  "SHARE_CAPITAL" numeric(20,2),
  "CAPITAL_RESERVE" numeric(20,2),
  "OTHER_COMPRE_INCOME" numeric(20,2),
  "SURPLUS_RESERVE" numeric(20,2),
  "UNASSIGN_RPOFIT" numeric(20,2),
  "TOTAL_PARENT_EQUITY" numeric(20,2),
  "MINORITY_EQUITY" numeric(20,2),
  "TOTAL_EQUITY" numeric(20,2),
  "TOTAL_LIAB_EQUITY" numeric(20,2),
  "MONETARYFUNDS_YOY" numeric(10,4),
  "TOTAL_ASSETS_YOY" numeric(10,4),
  "TOTAL_LIABILITIES_YOY" numeric(10,4),
  "TOTAL_EQUITY_YOY" numeric(10,4)
)
;

ALTER TABLE "public"."balance_sheet"
  OWNER TO "postgres";

CREATE INDEX "balance_sheet_REPORT_DATE_idx" ON "public"."balance_sheet" USING btree (
  "REPORT_DATE" "pg_catalog"."timestamptz_ops" DESC NULLS FIRST
);

COMMENT ON COLUMN "public"."balance_sheet"."SECUCODE" IS '证券代码';

COMMENT ON COLUMN "public"."balance_sheet"."SECURITY_CODE" IS '证券代码(纯数字)';

COMMENT ON COLUMN "public"."balance_sheet"."SECURITY_NAME_ABBR" IS '证券简称';

COMMENT ON COLUMN "public"."balance_sheet"."ORG_CODE" IS '机构代码';

COMMENT ON COLUMN "public"."balance_sheet"."ORG_TYPE" IS '机构类型';

COMMENT ON COLUMN "public"."balance_sheet"."REPORT_DATE" IS '报告日期';

COMMENT ON COLUMN "public"."balance_sheet"."REPORT_TYPE" IS '报告类型';

COMMENT ON COLUMN "public"."balance_sheet"."REPORT_DATE_NAME" IS '报告期名称';

COMMENT ON COLUMN "public"."balance_sheet"."SECURITY_TYPE_CODE" IS '证券类型代码';

COMMENT ON COLUMN "public"."balance_sheet"."NOTICE_DATE" IS '公告日期';

COMMENT ON COLUMN "public"."balance_sheet"."UPDATE_DATE" IS '数据更新日期';

COMMENT ON COLUMN "public"."balance_sheet"."CURRENCY" IS '货币代码';

COMMENT ON COLUMN "public"."balance_sheet"."MONETARYFUNDS" IS '货币资金';

COMMENT ON COLUMN "public"."balance_sheet"."NOTE_ACCOUNTS_RECE" IS '应收票据及应收账款';

COMMENT ON COLUMN "public"."balance_sheet"."NOTE_RECE" IS '其中:应收票据';

COMMENT ON COLUMN "public"."balance_sheet"."ACCOUNTS_RECE" IS '其中:应收账款';

COMMENT ON COLUMN "public"."balance_sheet"."PREPAYMENT" IS '预付款项';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_RECE" IS '其他应收款合计';

COMMENT ON COLUMN "public"."balance_sheet"."INVENTORY" IS '存货';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_CURRENT_ASSET" IS '其他流动资产';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_CURRENT_ASSETS" IS '流动资产合计';

COMMENT ON COLUMN "public"."balance_sheet"."LONG_EQUITY_INVEST" IS '长期股权投资';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_EQUITY_INVEST" IS '其他权益工具投资';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_NONCURRENT_FINASSET" IS '其他非流动金融资产';

COMMENT ON COLUMN "public"."balance_sheet"."INVEST_REALESTATE" IS '投资性房地产';

COMMENT ON COLUMN "public"."balance_sheet"."FIXED_ASSET" IS '固定资产';

COMMENT ON COLUMN "public"."balance_sheet"."CIP" IS '在建工程';

COMMENT ON COLUMN "public"."balance_sheet"."USERIGHT_ASSET" IS '使用权资产';

COMMENT ON COLUMN "public"."balance_sheet"."INTANGIBLE_ASSET" IS '无形资产';

COMMENT ON COLUMN "public"."balance_sheet"."LONG_PREPAID_EXPENSE" IS '长期待摊费用';

COMMENT ON COLUMN "public"."balance_sheet"."DEFER_TAX_ASSET" IS '递延所得税资产';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_NONCURRENT_ASSET" IS '其他非流动资产';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_NONCURRENT_ASSETS" IS '非流动资产合计';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_ASSETS" IS '资产总计';

COMMENT ON COLUMN "public"."balance_sheet"."SHORT_LOAN" IS '短期借款';

COMMENT ON COLUMN "public"."balance_sheet"."NOTE_ACCOUNTS_PAYABLE" IS '应付票据及应付账款';

COMMENT ON COLUMN "public"."balance_sheet"."NOTE_PAYABLE" IS '其中:应付票据';

COMMENT ON COLUMN "public"."balance_sheet"."ACCOUNTS_PAYABLE" IS '其中:应付账款';

COMMENT ON COLUMN "public"."balance_sheet"."ADVANCE_RECEIVABLES" IS '预收款项';

COMMENT ON COLUMN "public"."balance_sheet"."CONTRACT_LIAB" IS '合同负债';

COMMENT ON COLUMN "public"."balance_sheet"."STAFF_SALARY_PAYABLE" IS '应付职工薪酬';

COMMENT ON COLUMN "public"."balance_sheet"."TAX_PAYABLE" IS '应交税费';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_PAYABLE" IS '其他应付款合计';

COMMENT ON COLUMN "public"."balance_sheet"."DIVIDEND_PAYABLE" IS '其中:应付股利';

COMMENT ON COLUMN "public"."balance_sheet"."NONCURRENT_LIAB_1YEAR" IS '一年内到期的非流动负债';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_CURRENT_LIAB" IS '其他流动负债';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_CURRENT_LIAB" IS '流动负债合计';

COMMENT ON COLUMN "public"."balance_sheet"."LONG_LOAN" IS '长期借款';

COMMENT ON COLUMN "public"."balance_sheet"."LEASE_LIAB" IS '租赁负债';

COMMENT ON COLUMN "public"."balance_sheet"."LONG_PAYABLE" IS '长期应付款';

COMMENT ON COLUMN "public"."balance_sheet"."DEFER_TAX_LIAB" IS '递延所得税负债';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_NONCURRENT_LIAB" IS '其他非流动负债';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_NONCURRENT_LIAB" IS '非流动负债合计';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_LIABILITIES" IS '负债合计';

COMMENT ON COLUMN "public"."balance_sheet"."SHARE_CAPITAL" IS '实收资本(或股本)';

COMMENT ON COLUMN "public"."balance_sheet"."CAPITAL_RESERVE" IS '资本公积';

COMMENT ON COLUMN "public"."balance_sheet"."OTHER_COMPRE_INCOME" IS '其他综合收益';

COMMENT ON COLUMN "public"."balance_sheet"."SURPLUS_RESERVE" IS '盈余公积';

COMMENT ON COLUMN "public"."balance_sheet"."UNASSIGN_RPOFIT" IS '未分配利润';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_PARENT_EQUITY" IS '归属于母公司股东权益总计';

COMMENT ON COLUMN "public"."balance_sheet"."MINORITY_EQUITY" IS '少数股东权益';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_EQUITY" IS '股东权益合计';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_LIAB_EQUITY" IS '负债和股东权益总计';

COMMENT ON COLUMN "public"."balance_sheet"."MONETARYFUNDS_YOY" IS '货币资金同比增长率(%)';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_ASSETS_YOY" IS '资产总计同比增长率(%)';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_LIABILITIES_YOY" IS '负债合计同比增长率(%)';

COMMENT ON COLUMN "public"."balance_sheet"."TOTAL_EQUITY_YOY" IS '股东权益合计同比增长率(%)';

COMMENT ON TABLE "public"."balance_sheet" IS '资产负债表';
 */
@Data
public class StockBalanceSheet implements Serializable {
    private static final long serialVersionUID = 1L;
    private String secucode;// 证券代码
    private String securityCode;//证券代码(纯数字)
    private String securityNameAbbr;// 证券简称
    private String orgCode;// 机构代码
    private String orgType;// 机构类型
    private String reportDate;// 报告日期
    private String reportType;// 报告类型
    private String reportDateName;// 报告期名称
    private String securityTypeCode;// 证券类型代码
    private String noticeDate;// 公告日期
    private String updateDate;// 数据更新日期
    private String currency;// 货币代码
    private Double monetaryfunds;// 货币资金
    private Double noteAccountsRece;// 应收票据及应收账款
    private Double noteRece;// 其中:应收票据
    private Double accountsRece;// 其中:应收账款
    private Double prepayment;// 预付款项
    private Double otherRece;// 其他应收款合计
    private Double inventory;// 存货
    private Double otherCurrentAsset;// 其他流动资产
    private Double totalCurrentAssets;// 流动资产合计
    private Double longEquityInvest;// 长期股权投资
    private Double otherEquityInvest;// 其他权益工具投资
    private Double otherNoncurrentFinasst;// 其他非流动金融资产
    private Double investRealestate;// 投资性房地产
    private Double fixedAsset;// 固定资产
    private Double cip;// 在建工程
    private Double userightAsset;// 使用权资产
    private Double intangibleAsset;// 无形资产
    private Double longPrepaidExpense;// 长期待摊费用
    private Double deferTaxAsset;// 递延所得税资产
    private Double otherNoncurrentAsset;// 其他非流动资产
    private Double totalNoncurrentAssets;// 非流动资产合计
    private Double totalAssets;// 资产总计
    private Double shortLoan;// 短期借款
    private Double noteAccountsPayable;// 应付票据及应付账款
    private Double notePayable;// 其中:应付票据
    private Double accountsPayable;// 其中:应付账款
    private Double advanceReceivables;// 预收款项
    private Double contractLiab;// 合同负债
    private Double staffSalaryPayable;// 应付职工薪酬
    private Double taxPayable;// 应交税费
    private Double otherPayable;// 其他应付款合计
    private Double dividendPayable;// 应付股利
    private Double noncurrentLiab1year;// 一年内到期的非流动负债
    private Double otherCurrentLiab;// 其他流动负债
    private Double totalCurrentLiab;// 流动负债合计
    private Double longLoan;// 长期借款
    private Double leaseLiab;// 租赁负债
    private Double longPayable;// 长期应付款
    private Double deferTaxLiab;// 递延所得税负债
    private Double otherNoncurrentLiab;// 其他非流动负债
    private Double totalNoncurrentLiab;// 非流动负债合计
    private Double totalLiabilities;// 负债合计
    private Double shareCapital;// 实收资本(或股本)
    private Double capitalReserve;// 资本公积
    private Double otherCompreIncome;// 其他综合收益
    private Double surplusReserve;// 盈余公积
    private Double unassignRprofit;// 未分配利润
    private Double totalParentEquity;// 归属于母公司股东权益总计
    private Double minorityEquity;// 少数股东权益
    private Double totalEquity;// 股东权益合计
    private Double totalLiabEquity;// 负债和股东权益总计
    private Double monetaryfundsYoy;// 货币资金同比增长率(%)
    private Double totalAssetsYoy;// 资产总计同比增长率(%)
    private Double totalLiabilitiesYoy;// 负债合计同比增长率(%)
    private Double totalEquityYoy;// 股东权益合计同比增长率(%)
}
