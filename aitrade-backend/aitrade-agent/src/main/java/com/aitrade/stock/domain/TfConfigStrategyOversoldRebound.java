package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 超跌反弹策略回测参数配置对象 tf_config_strategy_oversold_rebound
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public class TfConfigStrategyOversoldRebound extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置主键ID */
    private Long id;

    /** 配置名称/版本备注（例如：超跌反弹_预设参数_v1） */
    @Excel(name = "配置名称/版本备注", readConverterExp = "例=如：超跌反弹_预设参数_v1")
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

    /** 短均线视窗（MA20） */
    @Excel(name = "短均线视窗", readConverterExp = "M=A20")
    private Long trendMaShortWindow;

    /** 长均线视窗（MA60） */
    @Excel(name = "长均线视窗", readConverterExp = "M=A60")
    private Long trendMaLongWindow;

    /** 20日乖离率阈值（<= -8%） */
    @Excel(name = "20日乖离率阈值", readConverterExp = "<==,-=8%")
    private BigDecimal biasMaxThreshold;

    /** RSI6超卖阈值 */
    @Excel(name = "RSI6超卖阈值")
    private BigDecimal momRsi6Threshold;

    /** RSI14超卖阈值 */
    @Excel(name = "RSI14超卖阈值")
    private BigDecimal momRsi14Threshold;

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
    private BigDecimal momKdjLowThreshold;

    /** 成交量均线视窗（Vol_MA5） */
    @Excel(name = "成交量均线视窗", readConverterExp = "V=ol_MA5")
    private Long vpVolMaWindow;

    /** 20日乖离率阈值（= 1.3倍） */
    @Excel(name = "20日乖离率阈值", readConverterExp = "==,1=.3倍")
    private BigDecimal vpMinVolRatio;

    /** 锤头线下影线倍数（&gt;= 2倍实体） */
    @Excel(name = "锤头线下影线倍数", readConverterExp = "&=gt;=,2=倍实体")
    private BigDecimal vpHammerShadowRatio;

    /** 最小涨幅（&gt;= 2%） */
    @Excel(name = "最小涨幅", readConverterExp = "&=gt;=,2=%")
    private BigDecimal vpMinChangeRate;

    /** 最小获利盘比例 */
    @Excel(name = "最小获利盘比例")
    private BigDecimal chipMinProfitRatio;

    /** 超跌乖离深度得分权重 */
    @Excel(name = "超跌乖离深度得分权重")
    private BigDecimal swBiasWeight;

    /** 量价反转力度权重 */
    @Excel(name = "量价反转力度权重")
    private BigDecimal swVolumeWeight;

    /** 动能修复权重 */
    @Excel(name = "动能修复权重")
    private BigDecimal swMomentumWeight;

    /** 筹码面权重 */
    @Excel(name = "筹码面权重")
    private BigDecimal swChipWeight;

    /** 乖离深度得分转换倍数 */
    @Excel(name = "乖离深度得分转换倍数")
    private BigDecimal biasScoreMultiplier;

    /** 乖离得分下限 */
    @Excel(name = "乖离得分下限")
    private BigDecimal biasScoreMin;

    /** 乖离得分上限 */
    @Excel(name = "乖离得分上限")
    private BigDecimal biasScoreMax;

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

    public void setBiasMaxThreshold(BigDecimal biasMaxThreshold) 
    {
        this.biasMaxThreshold = biasMaxThreshold;
    }

    public BigDecimal getBiasMaxThreshold() 
    {
        return biasMaxThreshold;
    }

    public void setMomRsi6Threshold(BigDecimal momRsi6Threshold) 
    {
        this.momRsi6Threshold = momRsi6Threshold;
    }

    public BigDecimal getMomRsi6Threshold() 
    {
        return momRsi6Threshold;
    }

    public void setMomRsi14Threshold(BigDecimal momRsi14Threshold) 
    {
        this.momRsi14Threshold = momRsi14Threshold;
    }

    public BigDecimal getMomRsi14Threshold() 
    {
        return momRsi14Threshold;
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

    public void setMomKdjLowThreshold(BigDecimal momKdjLowThreshold) 
    {
        this.momKdjLowThreshold = momKdjLowThreshold;
    }

    public BigDecimal getMomKdjLowThreshold() 
    {
        return momKdjLowThreshold;
    }

    public void setVpVolMaWindow(Long vpVolMaWindow) 
    {
        this.vpVolMaWindow = vpVolMaWindow;
    }

    public Long getVpVolMaWindow() 
    {
        return vpVolMaWindow;
    }

    public void setVpMinVolRatio(BigDecimal vpMinVolRatio) 
    {
        this.vpMinVolRatio = vpMinVolRatio;
    }

    public BigDecimal getVpMinVolRatio() 
    {
        return vpMinVolRatio;
    }

    public void setVpHammerShadowRatio(BigDecimal vpHammerShadowRatio) 
    {
        this.vpHammerShadowRatio = vpHammerShadowRatio;
    }

    public BigDecimal getVpHammerShadowRatio() 
    {
        return vpHammerShadowRatio;
    }

    public void setVpMinChangeRate(BigDecimal vpMinChangeRate) 
    {
        this.vpMinChangeRate = vpMinChangeRate;
    }

    public BigDecimal getVpMinChangeRate() 
    {
        return vpMinChangeRate;
    }

    public void setChipMinProfitRatio(BigDecimal chipMinProfitRatio) 
    {
        this.chipMinProfitRatio = chipMinProfitRatio;
    }

    public BigDecimal getChipMinProfitRatio() 
    {
        return chipMinProfitRatio;
    }

    public void setSwBiasWeight(BigDecimal swBiasWeight) 
    {
        this.swBiasWeight = swBiasWeight;
    }

    public BigDecimal getSwBiasWeight() 
    {
        return swBiasWeight;
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

    public void setBiasScoreMultiplier(BigDecimal biasScoreMultiplier) 
    {
        this.biasScoreMultiplier = biasScoreMultiplier;
    }

    public BigDecimal getBiasScoreMultiplier() 
    {
        return biasScoreMultiplier;
    }

    public void setBiasScoreMin(BigDecimal biasScoreMin) 
    {
        this.biasScoreMin = biasScoreMin;
    }

    public BigDecimal getBiasScoreMin() 
    {
        return biasScoreMin;
    }

    public void setBiasScoreMax(BigDecimal biasScoreMax) 
    {
        this.biasScoreMax = biasScoreMax;
    }

    public BigDecimal getBiasScoreMax() 
    {
        return biasScoreMax;
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
            .append("biasMaxThreshold", getBiasMaxThreshold())
            .append("momRsi6Threshold", getMomRsi6Threshold())
            .append("momRsi14Threshold", getMomRsi14Threshold())
            .append("momKdjN", getMomKdjN())
            .append("momKdjM1", getMomKdjM1())
            .append("momKdjM2", getMomKdjM2())
            .append("momKdjLowThreshold", getMomKdjLowThreshold())
            .append("vpVolMaWindow", getVpVolMaWindow())
            .append("vpMinVolRatio", getVpMinVolRatio())
            .append("vpHammerShadowRatio", getVpHammerShadowRatio())
            .append("vpMinChangeRate", getVpMinChangeRate())
            .append("chipMinProfitRatio", getChipMinProfitRatio())
            .append("swBiasWeight", getSwBiasWeight())
            .append("swVolumeWeight", getSwVolumeWeight())
            .append("swMomentumWeight", getSwMomentumWeight())
            .append("swChipWeight", getSwChipWeight())
            .append("biasScoreMultiplier", getBiasScoreMultiplier())
            .append("biasScoreMin", getBiasScoreMin())
            .append("biasScoreMax", getBiasScoreMax())
            .append("swDefaultMomentumScore", getSwDefaultMomentumScore())
            .append("swDefaultChipScore", getSwDefaultChipScore())
            .append("swSinglePeakBonus", getSwSinglePeakBonus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
