package com.aitrade.stock.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 回测记录对象 tf_backtest_record
 * 
 * @author aitrade
 * @date 2026-11-05
 */
public class TfBacktestRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 回测编码 */
    @Excel(name = "回测编码")
    private String code;

    /** 回测名称 */
    @Excel(name = "回测名称")
    private String name;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date fromDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date toDate;

    /** 主升浪策略配置ID */
    @Excel(name = "主升浪策略配置ID")
    private Long cnfStrongBreakoutId;

    /** 底部反转策略配置ID */
    @Excel(name = "底部反转策略配置ID")
    private Long cnfBottomPullbackId;

    /** 震荡攀升策略参数配置ID */
    @Excel(name = "震荡攀升策略参数配置ID")
    private Long cnfPullbackLowSuckId;

    /** 超跌反弹策略配置ID */
    @Excel(name = "超跌反弹策略配置ID")
    private Long cnfOversoldReboundId;

    /** 交易策略配置ID */
    @Excel(name = "交易策略配置ID")
    private Long cnfTradeId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCode(String code) 
    {
        this.code = code;
    }

    public String getCode() 
    {
        return code;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setFromDate(Date fromDate) 
    {
        this.fromDate = fromDate;
    }

    public Date getFromDate() 
    {
        return fromDate;
    }

    public void setToDate(Date toDate) 
    {
        this.toDate = toDate;
    }

    public Date getToDate() 
    {
        return toDate;
    }

    public void setCnfStrongBreakoutId(Long cnfStrongBreakoutId) 
    {
        this.cnfStrongBreakoutId = cnfStrongBreakoutId;
    }

    public Long getCnfStrongBreakoutId() 
    {
        return cnfStrongBreakoutId;
    }

    public void setCnfBottomPullbackId(Long cnfBottomPullbackId) 
    {
        this.cnfBottomPullbackId = cnfBottomPullbackId;
    }

    public Long getCnfBottomPullbackId() 
    {
        return cnfBottomPullbackId;
    }

    public void setCnfPullbackLowSuckId(Long cnfPullbackLowSuckId) 
    {
        this.cnfPullbackLowSuckId = cnfPullbackLowSuckId;
    }

    public Long getCnfPullbackLowSuckId() 
    {
        return cnfPullbackLowSuckId;
    }

    public void setCnfOversoldReboundId(Long cnfOversoldReboundId) 
    {
        this.cnfOversoldReboundId = cnfOversoldReboundId;
    }

    public Long getCnfOversoldReboundId() 
    {
        return cnfOversoldReboundId;
    }

    public void setCnfTradeId(Long cnfTradeId) 
    {
        this.cnfTradeId = cnfTradeId;
    }

    public Long getCnfTradeId() 
    {
        return cnfTradeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("code", getCode())
            .append("name", getName())
            .append("fromDate", getFromDate())
            .append("toDate", getToDate())
            .append("cnfStrongBreakoutId", getCnfStrongBreakoutId())
            .append("cnfBottomPullbackId", getCnfBottomPullbackId())
            .append("cnfPullbackLowSuckId", getCnfPullbackLowSuckId())
            .append("cnfOversoldReboundId", getCnfOversoldReboundId())
            .append("cnfTradeId", getCnfTradeId())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
