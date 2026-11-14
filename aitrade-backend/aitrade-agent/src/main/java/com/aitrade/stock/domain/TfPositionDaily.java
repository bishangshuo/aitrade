package com.aitrade.stock.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.aitrade.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 回测持仓日历对象 tf_position_daily
 * 
 * @author aitrade
 * @date 2026-11-12
 */
public class TfPositionDaily extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    private Long id;

    /** 回测主键 ID */
    private Long backtestId;

    /** 账户编号 */
    @Excel(name = "账户编号")
    private String accountNo;

    /** 股票代码（例如 002792.SZ） */
    @Excel(name = "股票代码", readConverterExp = "例=如,0=02792.SZ")
    private String symbol;

    /** 股票名称 */
    @Excel(name = "股票名称")
    private String stockName;

    /** 总持仓股数 */
    @Excel(name = "总持仓股数")
    private Long totalVolume;

    /** 可用股数（T+1：当天买入的为 0，次日解冻变为总股数） */
    @Excel(name = "可用股数", readConverterExp = "T=+1：当天买入的为,0=，次日解冻变为总股数")
    private Long availableVolume;

    /** 持仓成本价（加权平均成本） */
    @Excel(name = "持仓成本价", readConverterExp = "加=权平均成本")
    private BigDecimal costPrice;

    /** 当前最新价/收盘价 */
    @Excel(name = "当前最新价/收盘价")
    private BigDecimal currentPrice;

    /** 預測未來 N 天目標止盈價 */
    @Excel(name = "預測未來 N 天目標止盈價")
    private BigDecimal targetPrice; // 預測未來 N 天目標止盈價

    /** 建議動態止損價 */
    @Excel(name = "建議動態止損價")
    private BigDecimal stopLossPrice;  // 建議動態止損價

    /** 当前持仓市值（total_volume * current_price） */
    @Excel(name = "当前持仓市值", readConverterExp = "t=otal_volume,*=,c=urrent_price")
    private BigDecimal marketValue;

    /** 浮动盈亏金额 */
    @Excel(name = "浮动盈亏金额")
    private BigDecimal unrealizedPnl;

    /** 浮动盈亏比例 */
    @Excel(name = "浮动盈亏比例")
    private BigDecimal unrealizedPnlPct;

    /** 持仓日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "持仓日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date positionDate;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBacktestId(Long backtestId)
    {
        this.backtestId = backtestId;
    }

    public Long getBacktestId()
    {
        return backtestId;
    }

    public void setAccountNo(String accountNo) 
    {
        this.accountNo = accountNo;
    }

    public String getAccountNo() 
    {
        return accountNo;
    }

    public void setSymbol(String symbol) 
    {
        this.symbol = symbol;
    }

    public String getSymbol() 
    {
        return symbol;
    }

    public void setStockName(String stockName) 
    {
        this.stockName = stockName;
    }

    public String getStockName() 
    {
        return stockName;
    }

    public void setTotalVolume(Long totalVolume) 
    {
        this.totalVolume = totalVolume;
    }

    public Long getTotalVolume() 
    {
        return totalVolume;
    }

    public void setAvailableVolume(Long availableVolume) 
    {
        this.availableVolume = availableVolume;
    }

    public Long getAvailableVolume() 
    {
        return availableVolume;
    }

    public void setCostPrice(BigDecimal costPrice) 
    {
        this.costPrice = costPrice;
    }

    public BigDecimal getCostPrice() 
    {
        return costPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) 
    {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentPrice() 
    {
        return currentPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice)
    {
        this.targetPrice = targetPrice;
    }

    public BigDecimal getTargetPrice()
    {
        return targetPrice;
    }

    public void setStopLossPrice(BigDecimal stopLossPrice)
    {
        this.stopLossPrice = stopLossPrice;
    }

    public BigDecimal getStopLossPrice()
    {
        return stopLossPrice;
    }

    public void setMarketValue(BigDecimal marketValue) 
    {
        this.marketValue = marketValue;
    }

    public BigDecimal getMarketValue() 
    {
        return marketValue;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) 
    {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getUnrealizedPnl() 
    {
        return unrealizedPnl;
    }

    public void setUnrealizedPnlPct(BigDecimal unrealizedPnlPct) 
    {
        this.unrealizedPnlPct = unrealizedPnlPct;
    }

    public BigDecimal getUnrealizedPnlPct() 
    {
        return unrealizedPnlPct;
    }

    public void setPositionDate(Date positionDate) 
    {
        this.positionDate = positionDate;
    }

    public Date getPositionDate() 
    {
        return positionDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
                .append("backtestId", getBacktestId())
            .append("accountNo", getAccountNo())
            .append("symbol", getSymbol())
            .append("stockName", getStockName())
            .append("totalVolume", getTotalVolume())
            .append("availableVolume", getAvailableVolume())
            .append("costPrice", getCostPrice())
            .append("currentPrice", getCurrentPrice())
            .append("targetPrice", getTargetPrice())
            .append("stopLossPrice", getStopLossPrice())
            .append("marketValue", getMarketValue())
            .append("unrealizedPnl", getUnrealizedPnl())
            .append("unrealizedPnlPct", getUnrealizedPnlPct())
            .append("positionDate", getPositionDate())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
