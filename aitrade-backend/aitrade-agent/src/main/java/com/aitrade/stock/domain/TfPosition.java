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
 * 回測當前持倉对象 tf_position
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Data
public class TfPosition extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主鍵 ID */
    private Long id;

    /** 賬戶編號 */
    @Excel(name = "賬戶編號")
    private String accountNo;

    /** 股票代碼（例如 002792.SZ） */
    @Excel(name = "股票代碼", readConverterExp = "例=如,0=02792.SZ")
    private String symbol;

    /** 股票名稱 */
    @Excel(name = "股票名稱")
    private String stockName;

    /** 總持倉股數 */
    @Excel(name = "總持倉股數")
    private Long totalVolume;

    /** 可用股數（T+1：當天買入的為 0，次日解凍變為總股數） */
    @Excel(name = "可用股數", readConverterExp = "T=+1：當天買入的為,0=，次日解凍變為總股數")
    private Long availableVolume;

    /** 持倉成本價（加權平均成本） */
    @Excel(name = "持倉成本價", readConverterExp = "加=權平均成本")
    private BigDecimal costPrice;

    /** 當前最新價/收盤價 */
    @Excel(name = "當前最新價/收盤價")
    private BigDecimal currentPrice;

    /** 當前持倉市值（total_volume * current_price） */
    @Excel(name = "當前持倉市值", readConverterExp = "t=otal_volume,*=,c=urrent_price")
    private BigDecimal marketValue;

    /** 浮動盈虧金額 */
    @Excel(name = "浮動盈虧金額")
    private BigDecimal unrealizedPnl;

    /** 浮動盈虧比例 */
    @Excel(name = "浮動盈虧比例")
    private BigDecimal unrealizedPnlPct;

    /** 建倉日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "建倉日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date openDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("accountNo", getAccountNo())
            .append("symbol", getSymbol())
            .append("stockName", getStockName())
            .append("totalVolume", getTotalVolume())
            .append("availableVolume", getAvailableVolume())
            .append("costPrice", getCostPrice())
            .append("currentPrice", getCurrentPrice())
            .append("marketValue", getMarketValue())
            .append("unrealizedPnl", getUnrealizedPnl())
            .append("unrealizedPnlPct", getUnrealizedPnlPct())
            .append("openDate", getOpenDate())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
