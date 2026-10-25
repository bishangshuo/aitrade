package com.aitrade.stock.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.aitrade.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 回測資金流水明細对象 tf_fund_flow
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Data
public class TfFundFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主鍵 ID */
    private Long id;

    /** 賬戶編號 */
    @Excel(name = "賬戶編號")
    private String accountNo;

    /** 業務發生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "業務發生日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date tradeDate;

    /** 業務類型：DEPOSIT-入金, WITHDRAW-出金, BUY-買入扣款, SELL-賣出回款, COMMISSION-傭金, TAX-印花稅 */
    @Excel(name = "業務類型：DEPOSIT-入金, WITHDRAW-出金, BUY-買入扣款, SELL-賣出回款, COMMISSION-傭金, TAX-印花稅")
    private String bizType;

    /** 變動金額（收入為正，支出為負） */
    @Excel(name = "變動金額", readConverterExp = "收=入為正，支出為負")
    private BigDecimal amount;

    /** 變動後的可用現金餘額 */
    @Excel(name = "變動後的可用現金餘額")
    private BigDecimal balanceAfter;

    /** 關聯的訂單編號/流水號 */
    @Excel(name = "關聯的訂單編號/流水號")
    private String relatedOrderId;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("accountNo", getAccountNo())
            .append("tradeDate", getTradeDate())
            .append("bizType", getBizType())
            .append("amount", getAmount())
            .append("balanceAfter", getBalanceAfter())
            .append("relatedOrderId", getRelatedOrderId())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
