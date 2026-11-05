package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 震荡攀升低吸策略回测参数配置对象 tf_config_strategy_pullback_low_suck
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public class TfConfigStrategyPullbackLowSuck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置主键ID */
    private Long id;

    /** 配置名称/版本备注（例如：低吸策略_预设参数_v1） */
    @Excel(name = "配置名称/版本备注", readConverterExp = "例=如：低吸策略_预设参数_v1")
    private String configName;

    /** 策略编码 */
    @Excel(name = "策略编码")
    private Long strategyCode;

    /** 策略名称 */
    @Excel(name = "策略名称")
    private String strategyName;

    /** 最小需要的K线历史天数 */
    @Excel(name = "最小需要的K线历史天数")
    private Long minKlineCount;

    /** 短均线视窗（MA20） */
    @Excel(name = "短均线视窗", readConverterExp = "M=A20")
    private Long trendMaShortWindow;

    /** 长均线视窗（MA60） */
    @Excel(name = "长均线视窗", readConverterExp = "M=A60")
    private Long trendMaLongWindow;

    /** 前置均线斜率比较偏移天数 */
    @Excel(name = "前置均线斜率比较偏移天数")
    private Long trendPrevMaOffset;

    /** 近期低点回溯天数 */
    @Excel(name = "近期低点回溯天数")
    private Long trendLowRecentDays;

    /** 前置低点回溯天数 */
    @Excel(name = "前置低点回溯天数")
    private Long trendLowPriorDays;

    /** 成交量均线视窗（Vol_MA10） */
    @Excel(name = "成交量均线视窗", readConverterExp = "V=ol_MA10")
    private Long volMaWindow;

    /** 第一支撑均线（MA20） */
    @Excel(name = "第一支撑均线", readConverterExp = "M=A20")
    private Long supportMa1;

    /** 第二支撑均线（MA30） */
    @Excel(name = "第二支撑均线", readConverterExp = "M=A30")
    private Long supportMa2;

    /** 回踩支撑位最大误差（&lt;= 1.5%） */
    @Excel(name = "回踩支撑位最大误差", readConverterExp = "&=lt;=,1=.5%")
    private BigDecimal supportTolerance;

    /** 缩量回踩上限比例（< 75%） */
    @Excel(name = "缩量回踩上限比例", readConverterExp = "<=,7=5%")
    private BigDecimal maxVolRatioForPullback;

    /** 缩量回踩上限比例（= 1.5倍实体） */
    @Excel(name = "缩量回踩上限比例", readConverterExp = "==,1=.5倍实体")
    private BigDecimal hammerShadowRatio;

    /** KDJ参数N */
    @Excel(name = "KDJ参数N")
    private Long momKdjN;

    /** KDJ参数M1 */
    @Excel(name = "KDJ参数M1")
    private Long momKdjM1;

    /** KDJ参数M2 */
    @Excel(name = "KDJ参数M2")
    private Long momKdjM2;

    /** KDJ低位阈值 */
    @Excel(name = "KDJ低位阈值")
    private BigDecimal momKdjThreshold;

    /** RSI周期 */
    @Excel(name = "RSI周期")
    private Long rsiPeriod;

    /** RSI多头健康区下限 */
    @Excel(name = "RSI多头健康区下限")
    private BigDecimal rsiMin;

    /** RSI多头健康区上限 */
    @Excel(name = "RSI多头健康区上限")
    private BigDecimal rsiMax;

    /** MACD零轴贴近极限（&gt;= -0.02） */
    @Excel(name = "MACD零轴贴近极限", readConverterExp = "&=gt;=,-=0.02")
    private BigDecimal macdZeroLimit;

    /** 最小获利盘比例（低吸可适度放宽至 60%） */
    @Excel(name = "最小获利盘比例", readConverterExp = "低=吸可适度放宽至,6=0%")
    private BigDecimal chipMinProfitRatio;

    /** 趋势通道健康度权重 */
    @Excel(name = "趋势通道健康度权重")
    private BigDecimal swTrendWeight;

    /** 回踩缩量程度权重 */
    @Excel(name = "回踩缩量程度权重")
    private BigDecimal swShrinkWeight;

    /** 动能修复权重 */
    @Excel(name = "动能修复权重")
    private BigDecimal swMomentumWeight;

    /** 筹码面权重 */
    @Excel(name = "筹码面权重")
    private BigDecimal swChipWeight;

    /** 趋势强度得分转换倍数 */
    @Excel(name = "趋势强度得分转换倍数")
    private BigDecimal trendScoreMultiplier;

    /** 趋势得分下限 */
    @Excel(name = "趋势得分下限")
    private BigDecimal trendScoreMin;

    /** 趋势得分上限 */
    @Excel(name = "趋势得分上限")
    private BigDecimal trendScoreMax;

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

    public void setStrategyCode(Long strategyCode) 
    {
        this.strategyCode = strategyCode;
    }

    public Long getStrategyCode() 
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

    public void setTrendMaShortWindow(Long trendMaShortWindow) 
    {
        this.trendMaShortWindow = trendMaShortWindow;
    }

    public Long getTrendMaShortWindow() 
    {
        return trendMaShortWindow;
    }

    public void setTrendMaLongWindow(Long trendMaLongWindow) 
    {
        this.trendMaLongWindow = trendMaLongWindow;
    }

    public Long getTrendMaLongWindow() 
    {
        return trendMaLongWindow;
    }

    public void setTrendPrevMaOffset(Long trendPrevMaOffset) 
    {
        this.trendPrevMaOffset = trendPrevMaOffset;
    }

    public Long getTrendPrevMaOffset() 
    {
        return trendPrevMaOffset;
    }

    public void setTrendLowRecentDays(Long trendLowRecentDays) 
    {
        this.trendLowRecentDays = trendLowRecentDays;
    }

    public Long getTrendLowRecentDays() 
    {
        return trendLowRecentDays;
    }

    public void setTrendLowPriorDays(Long trendLowPriorDays) 
    {
        this.trendLowPriorDays = trendLowPriorDays;
    }

    public Long getTrendLowPriorDays() 
    {
        return trendLowPriorDays;
    }

    public void setVolMaWindow(Long volMaWindow) 
    {
        this.volMaWindow = volMaWindow;
    }

    public Long getVolMaWindow() 
    {
        return volMaWindow;
    }

    public void setSupportMa1(Long supportMa1) 
    {
        this.supportMa1 = supportMa1;
    }

    public Long getSupportMa1() 
    {
        return supportMa1;
    }

    public void setSupportMa2(Long supportMa2) 
    {
        this.supportMa2 = supportMa2;
    }

    public Long getSupportMa2() 
    {
        return supportMa2;
    }

    public void setSupportTolerance(BigDecimal supportTolerance) 
    {
        this.supportTolerance = supportTolerance;
    }

    public BigDecimal getSupportTolerance() 
    {
        return supportTolerance;
    }

    public void setMaxVolRatioForPullback(BigDecimal maxVolRatioForPullback) 
    {
        this.maxVolRatioForPullback = maxVolRatioForPullback;
    }

    public BigDecimal getMaxVolRatioForPullback() 
    {
        return maxVolRatioForPullback;
    }

    public void setHammerShadowRatio(BigDecimal hammerShadowRatio) 
    {
        this.hammerShadowRatio = hammerShadowRatio;
    }

    public BigDecimal getHammerShadowRatio() 
    {
        return hammerShadowRatio;
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

    public void setRsiPeriod(Long rsiPeriod) 
    {
        this.rsiPeriod = rsiPeriod;
    }

    public Long getRsiPeriod() 
    {
        return rsiPeriod;
    }

    public void setRsiMin(BigDecimal rsiMin) 
    {
        this.rsiMin = rsiMin;
    }

    public BigDecimal getRsiMin() 
    {
        return rsiMin;
    }

    public void setRsiMax(BigDecimal rsiMax) 
    {
        this.rsiMax = rsiMax;
    }

    public BigDecimal getRsiMax() 
    {
        return rsiMax;
    }

    public void setMacdZeroLimit(BigDecimal macdZeroLimit) 
    {
        this.macdZeroLimit = macdZeroLimit;
    }

    public BigDecimal getMacdZeroLimit() 
    {
        return macdZeroLimit;
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

    public void setSwShrinkWeight(BigDecimal swShrinkWeight) 
    {
        this.swShrinkWeight = swShrinkWeight;
    }

    public BigDecimal getSwShrinkWeight() 
    {
        return swShrinkWeight;
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

    public void setTrendScoreMultiplier(BigDecimal trendScoreMultiplier) 
    {
        this.trendScoreMultiplier = trendScoreMultiplier;
    }

    public BigDecimal getTrendScoreMultiplier() 
    {
        return trendScoreMultiplier;
    }

    public void setTrendScoreMin(BigDecimal trendScoreMin) 
    {
        this.trendScoreMin = trendScoreMin;
    }

    public BigDecimal getTrendScoreMin() 
    {
        return trendScoreMin;
    }

    public void setTrendScoreMax(BigDecimal trendScoreMax) 
    {
        this.trendScoreMax = trendScoreMax;
    }

    public BigDecimal getTrendScoreMax() 
    {
        return trendScoreMax;
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
            .append("trendMaShortWindow", getTrendMaShortWindow())
            .append("trendMaLongWindow", getTrendMaLongWindow())
            .append("trendPrevMaOffset", getTrendPrevMaOffset())
            .append("trendLowRecentDays", getTrendLowRecentDays())
            .append("trendLowPriorDays", getTrendLowPriorDays())
            .append("volMaWindow", getVolMaWindow())
            .append("supportMa1", getSupportMa1())
            .append("supportMa2", getSupportMa2())
            .append("supportTolerance", getSupportTolerance())
            .append("maxVolRatioForPullback", getMaxVolRatioForPullback())
            .append("hammerShadowRatio", getHammerShadowRatio())
            .append("momKdjN", getMomKdjN())
            .append("momKdjM1", getMomKdjM1())
            .append("momKdjM2", getMomKdjM2())
            .append("momKdjThreshold", getMomKdjThreshold())
            .append("rsiPeriod", getRsiPeriod())
            .append("rsiMin", getRsiMin())
            .append("rsiMax", getRsiMax())
            .append("macdZeroLimit", getMacdZeroLimit())
            .append("chipMinProfitRatio", getChipMinProfitRatio())
            .append("swTrendWeight", getSwTrendWeight())
            .append("swShrinkWeight", getSwShrinkWeight())
            .append("swMomentumWeight", getSwMomentumWeight())
            .append("swChipWeight", getSwChipWeight())
            .append("trendScoreMultiplier", getTrendScoreMultiplier())
            .append("trendScoreMin", getTrendScoreMin())
            .append("trendScoreMax", getTrendScoreMax())
            .append("swDefaultMomentumScore", getSwDefaultMomentumScore())
            .append("swDefaultChipScore", getSwDefaultChipScore())
            .append("swSinglePeakBonus", getSwSinglePeakBonus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
