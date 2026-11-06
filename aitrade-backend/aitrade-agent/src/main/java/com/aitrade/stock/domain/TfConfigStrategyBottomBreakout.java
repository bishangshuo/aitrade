package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 底部反转策略回测参数配置对象 tf_config_strategy_bottom_breakout
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public class TfConfigStrategyBottomBreakout extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置主键ID */
    private Long id;

    /** 配置名称/版本备注（例如：底部反转_预设参数_v1） */
    @Excel(name = "配置名称/版本备注", readConverterExp = "例=如：底部反转_预设参数_v1")
    private String configName;

    /** 策略编码 */
    @Excel(name = "策略编码")
    private Integer strategyCode;

    /** 策略名称 */
    @Excel(name = "策略名称")
    private String strategyName;

    /** 最小需要的K线历史天数 */
    @Excel(name = "最小需要的K线历史天数")
    private Long minKlineCount;

    /** 趋势回溯天数 */
    @Excel(name = "趋势回溯天数")
    private Long trendLookbackDays;

    /** 区间最高位置分位数（小于30%） */
    @Excel(name = "区间最高位置分位数", readConverterExp = "小=于30%")
    private BigDecimal trendMaxPositionRatio;

    /** 均线视窗大小 */
    @Excel(name = "均线视窗大小")
    private Long trendMaWindow;

    /** 前置均线偏移天数 */
    @Excel(name = "前置均线偏移天数")
    private Long trendPrevMaOffset;

    /** 均线止跌系数 */
    @Excel(name = "均线止跌系数")
    private BigDecimal trendMaFlatMultiplier;

    /** 最小涨幅（&gt;= 4%） */
    @Excel(name = "最小涨幅", readConverterExp = "&=gt;=,4=%")
    private BigDecimal vpMinChangeRate;

    /** 最小放量倍数（&gt;= 2.0倍） */
    @Excel(name = "最小放量倍数", readConverterExp = "&=gt;=,2=.0倍")
    private BigDecimal vpMinVolRatio;

    /** 成交量均线视窗 */
    @Excel(name = "成交量均线视窗")
    private Long vpVolMaWindow;

    /** 短均线视窗 */
    @Excel(name = "短均线视窗")
    private Long vpShortMaWindow;

    /** 长均线视窗 */
    @Excel(name = "长均线视窗")
    private Long vpLongMaWindow;

    /** KDJ参数N */
    @Excel(name = "KDJ参数N")
    private Long momKdjN;

    /** KDJ参数M1 */
    @Excel(name = "KDJ参数M1")
    private Long momKdjM1;

    /** KDJ参数M2 */
    @Excel(name = "KDJ参数M2")
    private Long momKdjM2;

    /** KDJ超卖阈值 */
    @Excel(name = "KDJ超卖阈值")
    private BigDecimal momKdjThreshold;

    /** RSI周期 */
    @Excel(name = "RSI周期")
    private Long momRsiPeriod;

    /** 昨日RSI上限 */
    @Excel(name = "昨日RSI上限")
    private BigDecimal momRsiYesterdayMax;

    /** 今日RSI下限 */
    @Excel(name = "今日RSI下限")
    private BigDecimal momRsiTodayMin;

    /** MACD背离回溯天数 */
    @Excel(name = "MACD背离回溯天数")
    private Long momMacdLookback;

    /** 最小获利盘比例 */
    @Excel(name = "最小获利盘比例")
    private BigDecimal chipMinProfitRatio;

    /** 趋势得分权重 */
    @Excel(name = "趋势得分权重")
    private BigDecimal swTrendWeight;

    /** 量价得分权重 */
    @Excel(name = "量价得分权重")
    private BigDecimal swVolumeWeight;

    /** 动能得分权重 */
    @Excel(name = "动能得分权重")
    private BigDecimal swMomentumWeight;

    /** 筹码得分权重 */
    @Excel(name = "筹码得分权重")
    private BigDecimal swChipWeight;

    /** 预设动能得分 */
    @Excel(name = "预设动能得分")
    private BigDecimal swDefaultMomentumScore;

    /** 预设筹码得分 */
    @Excel(name = "预设筹码得分")
    private BigDecimal swDefaultChipScore;

    /** 单峰密集加分 */
    @Excel(name = "单峰密集加分")
    private BigDecimal swSinglePeakBonus;

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

    public void setStrategyCode(Integer strategyCode)
    {
        this.strategyCode = strategyCode;
    }

    public Integer getStrategyCode()
    {
        return strategyCode;
    }

    public void setStrategyName(String strategyName) 
    {
        this.strategyName = strategyName;
    }

    public String getStrategyName() 
    {
        return strategyName;
    }

    public void setMinKlineCount(Long minKlineCount) 
    {
        this.minKlineCount = minKlineCount;
    }

    public Long getMinKlineCount() 
    {
        return minKlineCount;
    }

    public void setTrendLookbackDays(Long trendLookbackDays) 
    {
        this.trendLookbackDays = trendLookbackDays;
    }

    public Long getTrendLookbackDays() 
    {
        return trendLookbackDays;
    }

    public void setTrendMaxPositionRatio(BigDecimal trendMaxPositionRatio) 
    {
        this.trendMaxPositionRatio = trendMaxPositionRatio;
    }

    public BigDecimal getTrendMaxPositionRatio() 
    {
        return trendMaxPositionRatio;
    }

    public void setTrendMaWindow(Long trendMaWindow) 
    {
        this.trendMaWindow = trendMaWindow;
    }

    public Long getTrendMaWindow() 
    {
        return trendMaWindow;
    }

    public void setTrendPrevMaOffset(Long trendPrevMaOffset) 
    {
        this.trendPrevMaOffset = trendPrevMaOffset;
    }

    public Long getTrendPrevMaOffset() 
    {
        return trendPrevMaOffset;
    }

    public void setTrendMaFlatMultiplier(BigDecimal trendMaFlatMultiplier) 
    {
        this.trendMaFlatMultiplier = trendMaFlatMultiplier;
    }

    public BigDecimal getTrendMaFlatMultiplier() 
    {
        return trendMaFlatMultiplier;
    }

    public void setVpMinChangeRate(BigDecimal vpMinChangeRate) 
    {
        this.vpMinChangeRate = vpMinChangeRate;
    }

    public BigDecimal getVpMinChangeRate() 
    {
        return vpMinChangeRate;
    }

    public void setVpMinVolRatio(BigDecimal vpMinVolRatio) 
    {
        this.vpMinVolRatio = vpMinVolRatio;
    }

    public BigDecimal getVpMinVolRatio() 
    {
        return vpMinVolRatio;
    }

    public void setVpVolMaWindow(Long vpVolMaWindow) 
    {
        this.vpVolMaWindow = vpVolMaWindow;
    }

    public Long getVpVolMaWindow() 
    {
        return vpVolMaWindow;
    }

    public void setVpShortMaWindow(Long vpShortMaWindow) 
    {
        this.vpShortMaWindow = vpShortMaWindow;
    }

    public Long getVpShortMaWindow() 
    {
        return vpShortMaWindow;
    }

    public void setVpLongMaWindow(Long vpLongMaWindow) 
    {
        this.vpLongMaWindow = vpLongMaWindow;
    }

    public Long getVpLongMaWindow() 
    {
        return vpLongMaWindow;
    }

    public void setMomKdjN(Long momKdjN) 
    {
        this.momKdjN = momKdjN;
    }

    public Long getMomKdjN() 
    {
        return momKdjN;
    }

    public void setMomKdjM1(Long momKdjM1) 
    {
        this.momKdjM1 = momKdjM1;
    }

    public Long getMomKdjM1() 
    {
        return momKdjM1;
    }

    public void setMomKdjM2(Long momKdjM2) 
    {
        this.momKdjM2 = momKdjM2;
    }

    public Long getMomKdjM2() 
    {
        return momKdjM2;
    }

    public void setMomKdjThreshold(BigDecimal momKdjThreshold) 
    {
        this.momKdjThreshold = momKdjThreshold;
    }

    public BigDecimal getMomKdjThreshold() 
    {
        return momKdjThreshold;
    }

    public void setMomRsiPeriod(Long momRsiPeriod) 
    {
        this.momRsiPeriod = momRsiPeriod;
    }

    public Long getMomRsiPeriod() 
    {
        return momRsiPeriod;
    }

    public void setMomRsiYesterdayMax(BigDecimal momRsiYesterdayMax) 
    {
        this.momRsiYesterdayMax = momRsiYesterdayMax;
    }

    public BigDecimal getMomRsiYesterdayMax() 
    {
        return momRsiYesterdayMax;
    }

    public void setMomRsiTodayMin(BigDecimal momRsiTodayMin) 
    {
        this.momRsiTodayMin = momRsiTodayMin;
    }

    public BigDecimal getMomRsiTodayMin() 
    {
        return momRsiTodayMin;
    }

    public void setMomMacdLookback(Long momMacdLookback) 
    {
        this.momMacdLookback = momMacdLookback;
    }

    public Long getMomMacdLookback() 
    {
        return momMacdLookback;
    }

    public void setChipMinProfitRatio(BigDecimal chipMinProfitRatio) 
    {
        this.chipMinProfitRatio = chipMinProfitRatio;
    }

    public BigDecimal getChipMinProfitRatio() 
    {
        return chipMinProfitRatio;
    }

    public void setSwTrendWeight(BigDecimal swTrendWeight) 
    {
        this.swTrendWeight = swTrendWeight;
    }

    public BigDecimal getSwTrendWeight() 
    {
        return swTrendWeight;
    }

    public void setSwVolumeWeight(BigDecimal swVolumeWeight) 
    {
        this.swVolumeWeight = swVolumeWeight;
    }

    public BigDecimal getSwVolumeWeight() 
    {
        return swVolumeWeight;
    }

    public void setSwMomentumWeight(BigDecimal swMomentumWeight) 
    {
        this.swMomentumWeight = swMomentumWeight;
    }

    public BigDecimal getSwMomentumWeight() 
    {
        return swMomentumWeight;
    }

    public void setSwChipWeight(BigDecimal swChipWeight) 
    {
        this.swChipWeight = swChipWeight;
    }

    public BigDecimal getSwChipWeight() 
    {
        return swChipWeight;
    }

    public void setSwDefaultMomentumScore(BigDecimal swDefaultMomentumScore) 
    {
        this.swDefaultMomentumScore = swDefaultMomentumScore;
    }

    public BigDecimal getSwDefaultMomentumScore() 
    {
        return swDefaultMomentumScore;
    }

    public void setSwDefaultChipScore(BigDecimal swDefaultChipScore) 
    {
        this.swDefaultChipScore = swDefaultChipScore;
    }

    public BigDecimal getSwDefaultChipScore() 
    {
        return swDefaultChipScore;
    }

    public void setSwSinglePeakBonus(BigDecimal swSinglePeakBonus) 
    {
        this.swSinglePeakBonus = swSinglePeakBonus;
    }

    public BigDecimal getSwSinglePeakBonus() 
    {
        return swSinglePeakBonus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("configName", getConfigName())
            .append("strategyCode", getStrategyCode())
            .append("strategyName", getStrategyName())
            .append("minKlineCount", getMinKlineCount())
            .append("trendLookbackDays", getTrendLookbackDays())
            .append("trendMaxPositionRatio", getTrendMaxPositionRatio())
            .append("trendMaWindow", getTrendMaWindow())
            .append("trendPrevMaOffset", getTrendPrevMaOffset())
            .append("trendMaFlatMultiplier", getTrendMaFlatMultiplier())
            .append("vpMinChangeRate", getVpMinChangeRate())
            .append("vpMinVolRatio", getVpMinVolRatio())
            .append("vpVolMaWindow", getVpVolMaWindow())
            .append("vpShortMaWindow", getVpShortMaWindow())
            .append("vpLongMaWindow", getVpLongMaWindow())
            .append("momKdjN", getMomKdjN())
            .append("momKdjM1", getMomKdjM1())
            .append("momKdjM2", getMomKdjM2())
            .append("momKdjThreshold", getMomKdjThreshold())
            .append("momRsiPeriod", getMomRsiPeriod())
            .append("momRsiYesterdayMax", getMomRsiYesterdayMax())
            .append("momRsiTodayMin", getMomRsiTodayMin())
            .append("momMacdLookback", getMomMacdLookback())
            .append("chipMinProfitRatio", getChipMinProfitRatio())
            .append("swTrendWeight", getSwTrendWeight())
            .append("swVolumeWeight", getSwVolumeWeight())
            .append("swMomentumWeight", getSwMomentumWeight())
            .append("swChipWeight", getSwChipWeight())
            .append("swDefaultMomentumScore", getSwDefaultMomentumScore())
            .append("swDefaultChipScore", getSwDefaultChipScore())
            .append("swSinglePeakBonus", getSwSinglePeakBonus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
