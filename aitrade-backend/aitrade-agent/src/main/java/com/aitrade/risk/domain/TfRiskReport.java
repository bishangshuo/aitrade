package com.aitrade.risk.domain;

import lombok.Data;
import com.aitrade.common.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * AI研报对象 tf_risk_report
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Data
public class TfRiskReport implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 股票代码 */
    @Excel(name = "股票代码")
    private String stockCode;

    /** 股票名称 */
    @Excel(name = "股票名称")
    private String stockName;

    /** 市场类型：1-A股，2-港股，3-美股 */
    @Excel(name = "市场类型：1-A股，2-港股，3-美股")
    private Integer marketType;

    /** 风险公告ID */
    @Excel(name = "风险公告ID")
    private Long riskAnnId;

    /** AI输入参数 */
    @Excel(name = "AI输入参数")
    private String inputParams;

    /** AI输出结果 */
    @Excel(name = "AI输出结果")
    private String outputResponse;

    /** AI复核是否通过：0-未通过，1-通过 */
    @Excel(name = "AI复核是否通过：0-未通过，1-通过")
    private Integer passed;

    /** 备注 */
    @Excel(name = "备注")
    private String remarks;

    private Date createTime;


}
