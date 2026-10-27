package com.aitrade.risk.domain;

import lombok.Data;
import com.aitrade.common.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * 研报公告对象 tf_risk_report_ann
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Data
public class TfRiskReportAnn implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 风险研报ID */
    @Excel(name = "风险研报ID")
    private Long riskReportId;

    /** 风险公告ID */
    @Excel(name = "风险公告ID")
    private Long riskAnnId;

    private Date createTime;
}
