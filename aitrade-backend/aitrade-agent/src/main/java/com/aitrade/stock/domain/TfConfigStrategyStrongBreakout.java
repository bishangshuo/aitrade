package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 主升浪策略回测参数配置对象 tf_config_strategy_strong_breakout
 * 
 * @author aitrade
 * @date 2026-11-03
 */
public class TfConfigStrategyStrongBreakout extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置主键ID */
    private Long id;

    /** 配置版本名称（例如：主升浪_预设参数_v1） */
    @Excel(name = "配置版本名称", readConverterExp = "例=如：主升浪_预设参数_v1")
    private String configName;

    /** 策略编码 */
    @Excel(name = "策略编码")
    private Long strategyCode;

    /** 策略名称 */
    @Excel(name = "策略名称")
    private String strategyName;

    /** 最小K线数要求 */
    @Excel(name = "最小K线数要求")
    private Long minKlineCount;

    /** VCP回溯天数 */
    @Excel(name = "VCP回溯天数")
    private Long vcpLookbackDays;

    /** VCP最大振幅限制 */
    @Excel(name = "VCP最大振幅限制")
    private BigDecimal vcpMaxAmplitude;

    /** VCP最小涨幅 */
    @Excel(name = "VCP最小涨幅")
    private BigDecimal vcpMinChangeRate;

    /** VCP最小放量倍数 */
    @Excel(name = "VCP最小放量倍数")
    private BigDecimal vcpMinVolRatio;

    /** VCP突破前高回溯天数 */
    @Excel(name = "VCP突破前高回溯天数")
    private Long vcpHighLookback;

    /** 均线粘合极差比率上限 */
    @Excel(name = "均线粘合极差比率上限")
    private BigDecimal coilMaxDiffRatio;

    /** 空中加油回踩MA20容忍度 */
    @Excel(name = "空中加油回踩MA20容忍度")
    private BigDecimal airRefuelMaTolerance;

    /** MACD零轴贴近极限 */
    @Excel(name = "MACD零轴贴近极限")
    private BigDecimal macdNearZeroLimit;

    /** KDJ参数N */
    @Excel(name = "KDJ参数N")
    private Long kdjN;

    /** KDJ参数M1 */
    @Excel(name = "KDJ参数M1")
    private Long kdjM1;

    /** KDJ参数M2 */
    @Excel(name = "KDJ参数M2")
    private Long kdjM2;

    /** KDJ强势区间下限 */
    @Excel(name = "KDJ强势区间下限")
    private BigDecimal kdjMinStrong;

    /** KDJ强势区间上限 */
    @Excel(name = "KDJ强势区间上限")
    private BigDecimal kdjMaxStrong;

    /** RSI周期 */
    @Excel(name = "RSI周期")
    private Long rsiPeriod;

    /** RSI下限 */
    @Excel(name = "RSI下限")
    private BigDecimal rsiMin;

    /** RSI上限 */
    @Excel(name = "RSI上限")
    private BigDecimal rsiMax;

    /** OBV创新高回溯天数 */
    @Excel(name = "OBV创新高回溯天数")
    private Long obvLookbackDays;

    /** 最小获利盘比例 */
    @Excel(name = "最小获利盘比例")
    private BigDecimal chipMinProfitRatio;

    /** 量价爆发力权重 */
    @Excel(name = "量价爆发力权重")
    private BigDecimal swVolumeWeight;

    /** 筹码面权重 */
    @Excel(name = "筹码面权重")
    private BigDecimal swChipWeight;

    /** 技术动能权重 */
    @Excel(name = "技术动能权重")
    private BigDecimal swMomentumWeight;

    /** 形态品质权重 */
    @Excel(name = "形态品质权重")
    private BigDecimal swPatternWeight;

    /** 单峰密集加分 */
    @Excel(name = "单峰密集加分")
    private BigDecimal swSinglePeakBonus;

    /** MACD强红柱得分 */
    @Excel(name = "MACD强红柱得分")
    private BigDecimal swMacdHighScore;

    /** MACD一般红柱得分 */
    @Excel(name = "MACD一般红柱得分")
    private BigDecimal swMacdMidScore;

    /** 形态预设得分 */
    @Excel(name = "形态预设得分")
    private BigDecimal swPatternDefaultScore;

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

    public void setVcpLookbackDays(Long vcpLookbackDays) 
    {
        this.vcpLookbackDays = vcpLookbackDays;
    }

    public Long getVcpLookbackDays() 
    {
        return vcpLookbackDays;
    }

    public void setVcpMaxAmplitude(BigDecimal vcpMaxAmplitude) 
    {
        this.vcpMaxAmplitude = vcpMaxAmplitude;
    }

    public BigDecimal getVcpMaxAmplitude() 
    {
        return vcpMaxAmplitude;
    }

    public void setVcpMinChangeRate(BigDecimal vcpMinChangeRate) 
    {
        this.vcpMinChangeRate = vcpMinChangeRate;
    }

    public BigDecimal getVcpMinChangeRate() 
    {
        return vcpMinChangeRate;
    }

    public void setVcpMinVolRatio(BigDecimal vcpMinVolRatio) 
    {
        this.vcpMinVolRatio = vcpMinVolRatio;
    }

    public BigDecimal getVcpMinVolRatio() 
    {
        return vcpMinVolRatio;
    }

    public void setVcpHighLookback(Long vcpHighLookback) 
    {
        this.vcpHighLookback = vcpHighLookback;
    }

    public Long getVcpHighLookback() 
    {
        return vcpHighLookback;
    }

    public void setCoilMaxDiffRatio(BigDecimal coilMaxDiffRatio) 
    {
        this.coilMaxDiffRatio = coilMaxDiffRatio;
    }

    public BigDecimal getCoilMaxDiffRatio() 
    {
        return coilMaxDiffRatio;
    }

    public void setAirRefuelMaTolerance(BigDecimal airRefuelMaTolerance) 
    {
        this.airRefuelMaTolerance = airRefuelMaTolerance;
    }

    public BigDecimal getAirRefuelMaTolerance() 
    {
        return airRefuelMaTolerance;
    }

    public void setMacdNearZeroLimit(BigDecimal macdNearZeroLimit) 
    {
        this.macdNearZeroLimit = macdNearZeroLimit;
    }

    public BigDecimal getMacdNearZeroLimit() 
    {
        return macdNearZeroLimit;
    }

    public void setKdjN(Long kdjN) 
    {
        this.kdjN = kdjN;
    }

    public Long getKdjN() 
    {
        return kdjN;
    }

    public void setKdjM1(Long kdjM1) 
    {
        this.kdjM1 = kdjM1;
    }

    public Long getKdjM1() 
    {
        return kdjM1;
    }

    public void setKdjM2(Long kdjM2) 
    {
        this.kdjM2 = kdjM2;
    }

    public Long getKdjM2() 
    {
        return kdjM2;
    }

    public void setKdjMinStrong(BigDecimal kdjMinStrong) 
    {
        this.kdjMinStrong = kdjMinStrong;
    }

    public BigDecimal getKdjMinStrong() 
    {
        return kdjMinStrong;
    }

    public void setKdjMaxStrong(BigDecimal kdjMaxStrong) 
    {
        this.kdjMaxStrong = kdjMaxStrong;
    }

    public BigDecimal getKdjMaxStrong() 
    {
        return kdjMaxStrong;
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

    public void setObvLookbackDays(Long obvLookbackDays) 
    {
        this.obvLookbackDays = obvLookbackDays;
    }

    public Long getObvLookbackDays() 
    {
        return obvLookbackDays;
    }

    public void setChipMinProfitRatio(BigDecimal chipMinProfitRatio) 
    {
        this.chipMinProfitRatio = chipMinProfitRatio;
    }

    public BigDecimal getChipMinProfitRatio() 
    {
        return chipMinProfitRatio;
    }

    public void setSwVolumeWeight(BigDecimal swVolumeWeight) 
    {
        this.swVolumeWeight = swVolumeWeight;
    }

    public BigDecimal getSwVolumeWeight() 
    {
        return swVolumeWeight;
    }

    public void setSwChipWeight(BigDecimal swChipWeight) 
    {
        this.swChipWeight = swChipWeight;
    }

    public BigDecimal getSwChipWeight() 
    {
        return swChipWeight;
    }

    public void setSwMomentumWeight(BigDecimal swMomentumWeight) 
    {
        this.swMomentumWeight = swMomentumWeight;
    }

    public BigDecimal getSwMomentumWeight() 
    {
        return swMomentumWeight;
    }

    public void setSwPatternWeight(BigDecimal swPatternWeight) 
    {
        this.swPatternWeight = swPatternWeight;
    }

    public BigDecimal getSwPatternWeight() 
    {
        return swPatternWeight;
    }

    public void setSwSinglePeakBonus(BigDecimal swSinglePeakBonus) 
    {
        this.swSinglePeakBonus = swSinglePeakBonus;
    }

    public BigDecimal getSwSinglePeakBonus() 
    {
        return swSinglePeakBonus;
    }

    public void setSwMacdHighScore(BigDecimal swMacdHighScore) 
    {
        this.swMacdHighScore = swMacdHighScore;
    }

    public BigDecimal getSwMacdHighScore() 
    {
        return swMacdHighScore;
    }

    public void setSwMacdMidScore(BigDecimal swMacdMidScore) 
    {
        this.swMacdMidScore = swMacdMidScore;
    }

    public BigDecimal getSwMacdMidScore() 
    {
        return swMacdMidScore;
    }

    public void setSwPatternDefaultScore(BigDecimal swPatternDefaultScore) 
    {
        this.swPatternDefaultScore = swPatternDefaultScore;
    }

    public BigDecimal getSwPatternDefaultScore() 
    {
        return swPatternDefaultScore;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("configName", getConfigName())
            .append("strategyCode", getStrategyCode())
            .append("strategyName", getStrategyName())
            .append("minKlineCount", getMinKlineCount())
            .append("vcpLookbackDays", getVcpLookbackDays())
            .append("vcpMaxAmplitude", getVcpMaxAmplitude())
            .append("vcpMinChangeRate", getVcpMinChangeRate())
            .append("vcpMinVolRatio", getVcpMinVolRatio())
            .append("vcpHighLookback", getVcpHighLookback())
            .append("coilMaxDiffRatio", getCoilMaxDiffRatio())
            .append("airRefuelMaTolerance", getAirRefuelMaTolerance())
            .append("macdNearZeroLimit", getMacdNearZeroLimit())
            .append("kdjN", getKdjN())
            .append("kdjM1", getKdjM1())
            .append("kdjM2", getKdjM2())
            .append("kdjMinStrong", getKdjMinStrong())
            .append("kdjMaxStrong", getKdjMaxStrong())
            .append("rsiPeriod", getRsiPeriod())
            .append("rsiMin", getRsiMin())
            .append("rsiMax", getRsiMax())
            .append("obvLookbackDays", getObvLookbackDays())
            .append("chipMinProfitRatio", getChipMinProfitRatio())
            .append("swVolumeWeight", getSwVolumeWeight())
            .append("swChipWeight", getSwChipWeight())
            .append("swMomentumWeight", getSwMomentumWeight())
            .append("swPatternWeight", getSwPatternWeight())
            .append("swSinglePeakBonus", getSwSinglePeakBonus())
            .append("swMacdHighScore", getSwMacdHighScore())
            .append("swMacdMidScore", getSwMacdMidScore())
            .append("swPatternDefaultScore", getSwPatternDefaultScore())
            .append("createTime", getCreateTime())
            .toString();
    }
}
