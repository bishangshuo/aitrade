package com.aitrade.stock.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.aitrade.common.core.domain.BaseEntity;
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

    /** 账户编号 */
    @Excel(name = "账户编号")
    private String accountNo;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date fromDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date toDate;

    /** 初始资金 */
    @Excel(name = "初始资金")
    private BigDecimal initialCash;

    /** 主升浪策略配置ID */
    @Excel(name = "主升浪策略配置ID")
    private Long cnfStrongBreakoutId;

    private TfConfigStrategyStrongBreakout cnfStrongBreakout;

    /** 底部反转策略配置ID */
    @Excel(name = "底部反转策略配置ID")
    private Long cnfBottomBreakoutId;

    private TfConfigStrategyBottomBreakout cnfBottomBreakout;

    /** 震荡攀升策略参数配置ID */
    @Excel(name = "震荡攀升策略参数配置ID")
    private Long cnfPullbackLowSuckId;

    private TfConfigStrategyPullbackLowSuck cnfPullbackLowSuck;

    /** 超跌反弹策略配置ID */
    @Excel(name = "超跌反弹策略配置ID")
    private Long cnfOversoldReboundId;

    private TfConfigStrategyOversoldRebound cnfOversoldRebound;

    /** 交易策略配置ID */
    @Excel(name = "交易策略配置ID")
    private Long cnfTradeId;

    private TfConfigStrategyTrade cnfTrade;

    /**当前已完成日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date finishedDate;

    /**
     * 状态0未开始1正在进行2已完成3已终止
     */
    @Excel(name = "状态", readConverterExp = "0=未开始,1=正在进行,2=已完成,3=已终止")
    private Integer status;

    /** 是否使用AI 0不使用1使用 */
    private Integer useAi;

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

    public void setAccountNo(String accountNo)
    {
        this.accountNo = accountNo;
    }

    public String getAccountNo()
    {
        return accountNo;
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

    public void setInitialCash(BigDecimal initialCash)
    {
        this.initialCash = initialCash;
    }

    public BigDecimal getInitialCash()
    {
        return initialCash;
    }

    public void setCnfStrongBreakoutId(Long cnfStrongBreakoutId) 
    {
        this.cnfStrongBreakoutId = cnfStrongBreakoutId;
    }

    public Long getCnfStrongBreakoutId() 
    {
        return cnfStrongBreakoutId;
    }

    public TfConfigStrategyStrongBreakout getCnfStrongBreakout()
    {
        return cnfStrongBreakout;
    }

    public void setCnfStrongBreakout(TfConfigStrategyStrongBreakout cnfStrongBreakout)
    {
        this.cnfStrongBreakout = cnfStrongBreakout;
    }

    public void setCnfBottomBreakoutId(Long cnfBottomBreakoutId)
    {
        this.cnfBottomBreakoutId = cnfBottomBreakoutId;
    }

    public Long getCnfBottomBreakoutId()
    {
        return cnfBottomBreakoutId;
    }

    public TfConfigStrategyBottomBreakout getCnfBottomBreakout()
    {
        return cnfBottomBreakout;
    }

    public void setCnfBottomBreakout(TfConfigStrategyBottomBreakout cnfBottomBreakout)
    {
        this.cnfBottomBreakout = cnfBottomBreakout;
    }

    public void setCnfPullbackLowSuckId(Long cnfPullbackLowSuckId) 
    {
        this.cnfPullbackLowSuckId = cnfPullbackLowSuckId;
    }

    public Long getCnfPullbackLowSuckId() 
    {
        return cnfPullbackLowSuckId;
    }

    public TfConfigStrategyPullbackLowSuck getCnfPullbackLowSuck()
    {
        return cnfPullbackLowSuck;
    }

    public void setCnfPullbackLowSuck(TfConfigStrategyPullbackLowSuck cnfPullbackLowSuck)
    {
        this.cnfPullbackLowSuck = cnfPullbackLowSuck;
    }

    public void setCnfOversoldReboundId(Long cnfOversoldReboundId) 
    {
        this.cnfOversoldReboundId = cnfOversoldReboundId;
    }

    public Long getCnfOversoldReboundId() 
    {
        return cnfOversoldReboundId;
    }

    public TfConfigStrategyOversoldRebound getCnfOversoldRebound()
    {
        return cnfOversoldRebound;
    }

    public void setCnfOversoldRebound(TfConfigStrategyOversoldRebound cnfOversoldRebound)
    {
        this.cnfOversoldRebound = cnfOversoldRebound;
    }

    public void setCnfTradeId(Long cnfTradeId) 
    {
        this.cnfTradeId = cnfTradeId;
    }

    public Long getCnfTradeId() 
    {
        return cnfTradeId;
    }

    public TfConfigStrategyTrade getCnfTrade()
    {
        return cnfTrade;
    }

    public void setCnfTrade(TfConfigStrategyTrade cnfTrade)
    {
        this.cnfTrade = cnfTrade;
    }

    public void setUseAi(Integer useAi)
    {
        this.useAi = useAi;
    }

    public Integer getUseAi()
    {
        return useAi;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("code", getCode())
            .append("name", getName())
            .append("accountNo", getAccountNo())
            .append("fromDate", getFromDate())
            .append("toDate", getToDate())
            .append("cnfStrongBreakoutId", getCnfStrongBreakoutId())
            .append("cnfStrongBreakout", getCnfStrongBreakout().toString())
            .append("cnfBottomBreakoutId", getCnfBottomBreakoutId())
            .append("cnfBottomBreakout", getCnfBottomBreakout().toString())
            .append("cnfPullbackLowSuckId", getCnfPullbackLowSuckId())
            .append("cnfPullbackLowSuck", getCnfPullbackLowSuck().toString())
            .append("cnfOversoldReboundId", getCnfOversoldReboundId())
            .append("cnfOversoldRebound", getCnfOversoldRebound().toString())
            .append("cnfTradeId", getCnfTradeId())
            .append("cnfTrade", getCnfTrade().toString())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("useAi", getUseAi())
            .toString();
    }
}
