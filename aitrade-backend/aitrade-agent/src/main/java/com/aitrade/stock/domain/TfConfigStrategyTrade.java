package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 交易策略参数配置对象 tf_config_strategy_trade
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public class TfConfigStrategyTrade extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String configName;

    /** T+1高开幅度上限 */
    @Excel(name = "T+1高开幅度上限")
    private BigDecimal t1HighOpenRatio;

    /** 持仓天数 */
    @Excel(name = "持仓天数")
    private Long holdDays;

    /** 止损方法:1固定幅度，2动态幅度 */
    @Excel(name = "止损方法:1固定幅度，2动态幅度")
    private Integer stopLossMethod;

    /** 止损幅度 */
    @Excel(name = "止损幅度")
    private BigDecimal stopLossRatio;

    /** 止盈方法:1固定幅度，2动态幅度 */
    @Excel(name = "止盈方法:1固定幅度，2动态幅度")
    private Integer stopProfitMethod;

    /** 止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算 */
    @Excel(name = "止盈幅度:如果止盈方法为2，则止盈幅度由k线来动态计算")
    private BigDecimal stopProfitRatio;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }

    public String getConfigName()
    {
        return configName;
    }

    public void setT1HighOpenRatio(BigDecimal t1HighOpenRatio) 
    {
        this.t1HighOpenRatio = t1HighOpenRatio;
    }

    public BigDecimal getT1HighOpenRatio() 
    {
        return t1HighOpenRatio;
    }

    public void setHoldDays(Long holdDays) 
    {
        this.holdDays = holdDays;
    }

    public Long getHoldDays() 
    {
        return holdDays;
    }

    public void setStopLossMethod(Integer stopLossMethod)
    {
        this.stopLossMethod = stopLossMethod;
    }

    public Integer getStopLossMethod()
    {
        return stopLossMethod;
    }

    public void setStopLossRatio(BigDecimal stopLossRatio) 
    {
        this.stopLossRatio = stopLossRatio;
    }

    public BigDecimal getStopLossRatio() 
    {
        return stopLossRatio;
    }

    public void setStopProfitMethod(Integer stopProfitMethod) 
    {
        this.stopProfitMethod = stopProfitMethod;
    }

    public Integer getStopProfitMethod() 
    {
        return stopProfitMethod;
    }

    public void setStopProfitRatio(BigDecimal stopProfitRatio) 
    {
        this.stopProfitRatio = stopProfitRatio;
    }

    public BigDecimal getStopProfitRatio() 
    {
        return stopProfitRatio;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("configName", getConfigName())
            .append("t1HighOpenRatio", getT1HighOpenRatio())
            .append("holdDays", getHoldDays())
            .append("stopLossMethod", getStopLossMethod())
            .append("stopLossRatio", getStopLossRatio())
            .append("stopProfitMethod", getStopProfitMethod())
            .append("stopProfitRatio", getStopProfitRatio())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
