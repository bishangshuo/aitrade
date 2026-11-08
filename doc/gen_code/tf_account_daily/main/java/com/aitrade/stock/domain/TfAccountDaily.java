package com.aitrade.stock.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 回测账户快照对象 tf_account_daily
 * 
 * @author aitrade
 * @date 2026-11-08
 */
public class TfAccountDaily extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 账户编号 */
    @Excel(name = "账户编号")
    private String accountNo;

    /** 日历日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日历日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date calenderDate;

    /** 可用现金（可用于买入股票） */
    @Excel(name = "可用现金", readConverterExp = "可=用于买入股票")
    private BigDecimal availableCash;

    /** 冻结现金（挂单未成交或手续费预冻结） */
    @Excel(name = "冻结现金", readConverterExp = "挂=单未成交或手续费预冻结")
    private BigDecimal frozenCash;

    /** 持仓市值 */
    @Excel(name = "持仓市值")
    private BigDecimal marketValue;

    /** 总资产（可用现金 + 冻结现金 + 持仓市值） */
    @Excel(name = "总资产", readConverterExp = "可=用现金,+=,冻=结现金,+=,持=仓市值")
    private BigDecimal totalAssets;

    /** 累计盈亏金额 */
    @Excel(name = "累计盈亏金额")
    private BigDecimal totalPnl;

    /** 累计盈亏比例（收益率） */
    @Excel(name = "累计盈亏比例", readConverterExp = "收=益率")
    private BigDecimal totalPnlPct;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setAccountNo(String accountNo) 
    {
        this.accountNo = accountNo;
    }

    public String getAccountNo() 
    {
        return accountNo;
    }

    public void setCalenderDate(Date calenderDate) 
    {
        this.calenderDate = calenderDate;
    }

    public Date getCalenderDate() 
    {
        return calenderDate;
    }

    public void setAvailableCash(BigDecimal availableCash) 
    {
        this.availableCash = availableCash;
    }

    public BigDecimal getAvailableCash() 
    {
        return availableCash;
    }

    public void setFrozenCash(BigDecimal frozenCash) 
    {
        this.frozenCash = frozenCash;
    }

    public BigDecimal getFrozenCash() 
    {
        return frozenCash;
    }

    public void setMarketValue(BigDecimal marketValue) 
    {
        this.marketValue = marketValue;
    }

    public BigDecimal getMarketValue() 
    {
        return marketValue;
    }

    public void setTotalAssets(BigDecimal totalAssets) 
    {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getTotalAssets() 
    {
        return totalAssets;
    }

    public void setTotalPnl(BigDecimal totalPnl) 
    {
        this.totalPnl = totalPnl;
    }

    public BigDecimal getTotalPnl() 
    {
        return totalPnl;
    }

    public void setTotalPnlPct(BigDecimal totalPnlPct) 
    {
        this.totalPnlPct = totalPnlPct;
    }

    public BigDecimal getTotalPnlPct() 
    {
        return totalPnlPct;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("accountNo", getAccountNo())
            .append("calenderDate", getCalenderDate())
            .append("availableCash", getAvailableCash())
            .append("frozenCash", getFrozenCash())
            .append("marketValue", getMarketValue())
            .append("totalAssets", getTotalAssets())
            .append("totalPnl", getTotalPnl())
            .append("totalPnlPct", getTotalPnlPct())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
