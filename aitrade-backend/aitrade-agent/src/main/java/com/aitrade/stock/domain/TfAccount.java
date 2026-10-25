package com.aitrade.stock.domain;

import java.math.BigDecimal;

import com.aitrade.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 回測虛擬賬戶对象 tf_account
 * 
 * @author aitrade
 * @date 2026-10-25
 */
@Data
public class TfAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主鍵 ID */
    private Long id;

    /** 賬戶編號/策略標識 */
    @Excel(name = "賬戶編號/策略標識")
    private String accountNo;

    /** 初始資金 */
    @Excel(name = "初始資金")
    private BigDecimal initialCapital;

    /** 可用現金（可用於買入股票） */
    @Excel(name = "可用現金", readConverterExp = "可=用於買入股票")
    private BigDecimal availableCash;

    /** 凍結現金（掛單未成交或手續費預凍結） */
    @Excel(name = "凍結現金", readConverterExp = "掛=單未成交或手續費預凍結")
    private BigDecimal frozenCash;

    /** 持倉市值 */
    @Excel(name = "持倉市值")
    private BigDecimal marketValue;

    /** 總資產（可用現金 + 凍結現金 + 持倉市值） */
    @Excel(name = "總資產", readConverterExp = "可=用現金,+=,凍=結現金,+=,持=倉市值")
    private BigDecimal totalAssets;

    /** 累計盈虧金額 */
    @Excel(name = "累計盈虧金額")
    private BigDecimal totalPnl;

    /** 累計盈虧比例（收益率） */
    @Excel(name = "累計盈虧比例", readConverterExp = "收=益率")
    private BigDecimal totalPnlPct;

    /** 狀態：1-正常，0-凍結 */
    @Excel(name = "狀態：1-正常，0-凍結")
    private Integer status;



    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("accountNo", getAccountNo())
            .append("initialCapital", getInitialCapital())
            .append("availableCash", getAvailableCash())
            .append("frozenCash", getFrozenCash())
            .append("marketValue", getMarketValue())
            .append("totalAssets", getTotalAssets())
            .append("totalPnl", getTotalPnl())
            .append("totalPnlPct", getTotalPnlPct())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
