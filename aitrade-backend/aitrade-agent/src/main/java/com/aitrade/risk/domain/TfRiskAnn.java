package com.aitrade.risk.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 风险公告对象 tf_risk_ann
 * 
 * @author aitrade
 * @date 2026-10-27
 */
@Data
public class TfRiskAnn implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishTime;

    /** 股票代号 */
    @Excel(name = "股票代号")
    private String stockCode;

    /** 股票名称 */
    @Excel(name = "股票名称")
    private String stockName;

    /** 市场代码 */
    @Excel(name = "市场代码")
    private String marketCode;

    /** 公告类型 */
    @Excel(name = "公告类型")
    private String annType;

    /** 消息类型 */
    @Excel(name = "消息类型")
    private String sourceType;

    /** 文章编码 */
    @Excel(name = "文章编码")
    private String artCode;

    /** 公告标题 */
    @Excel(name = "公告标题")
    private String title;

    /** 公告内容 */
    @Excel(name = "公告内容")
    private String content;

    /** 公告文件url */
    @Excel(name = "公告文件url")
    private String fileUrl;

    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
