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
 * 回測歷史成交交割單对象 tf_trade_execution
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Data
public class TfTradeExecution extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主鍵 ID */
    private Long id;

    /** 賬戶編號 */
    @Excel(name = "賬戶編號")
    private String accountNo;

    /** 訂單編號 */
    @Excel(name = "訂單編號")
    private String orderId;

    /** 股票代碼 */
    @Excel(name = "股票代碼")
    private String symbol;

    /** 股票名稱 */
    @Excel(name = "股票名稱")
    private String stockName;

    /** 交易方向：BUY-買入, SELL-賣出 */
    @Excel(name = "交易方向：BUY-買入, SELL-賣出")
    private String tradeSide;

    /** 成交價格 */
    @Excel(name = "成交價格")
    private BigDecimal tradePrice;

    /** 成交股數 */
    @Excel(name = "成交股數")
    private Long tradeVolume;

    /** 成交總金額（價格 * 股數） */
    @Excel(name = "成交總金額", readConverterExp = "價=格,*=,股=數")
    private BigDecimal tradeAmount;

    /** 交易傭金 */
    @Excel(name = "交易傭金")
    private BigDecimal commission;

    /** 印花稅（僅賣出時收取） */
    @Excel(name = "印花稅", readConverterExp = "僅=賣出時收取")
    private BigDecimal tax;

    /** 平倉實現盈虧（僅賣出時計算） */
    @Excel(name = "平倉實現盈虧", readConverterExp = "僅=賣出時計算")
    private BigDecimal realizedPnl;

    /** 成交時間（回測中的模擬撮合時間點） */
    @Excel(name = "成交時間", readConverterExp = "回=測中的模擬撮合時間點")
    private Date tradeTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("accountNo", getAccountNo())
            .append("orderId", getOrderId())
            .append("symbol", getSymbol())
            .append("stockName", getStockName())
            .append("tradeSide", getTradeSide())
            .append("tradePrice", getTradePrice())
            .append("tradeVolume", getTradeVolume())
            .append("tradeAmount", getTradeAmount())
            .append("commission", getCommission())
            .append("tax", getTax())
            .append("realizedPnl", getRealizedPnl())
            .append("tradeTime", getTradeTime())
            .append("createTime", getCreateTime())
            .toString();
    }
}
