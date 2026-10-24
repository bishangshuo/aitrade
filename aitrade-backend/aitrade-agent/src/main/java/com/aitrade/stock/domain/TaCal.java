package com.aitrade.stock.domain;

import java.util.Date;

import com.aitrade.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 交易日历对象 ta_cal
 * 
 * @author bishangshuo
 * @date 2026-10-19
 */
public class TaCal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 日历日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日历日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date calDate;

    /** 是否开始1开始0修士 */
    @Excel(name = "是否开始1开始0修士")
    private Integer isOpen;

    /** 礼拜名称1-7 */
    @Excel(name = "礼拜名称1-7")
    private Integer weekDay;

    /** 开始时间 */
    private Date beginDate;
    /** 结束时间 */
    private Date endDate;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCalDate(Date calDate) 
    {
        this.calDate = calDate;
    }

    public Date getCalDate() 
    {
        return calDate;
    }

    public void setIsOpen(Integer isOpen) 
    {
        this.isOpen = isOpen;
    }

    public Integer getIsOpen() 
    {
        return isOpen;
    }

    public void setWeekDay(Integer weekDay) 
    {
        this.weekDay = weekDay;
    }

    public Integer getWeekDay() 
    {
        return weekDay;
    }

    public void setBeginDate(Date beginDate)
    {
        this.beginDate = beginDate;
    }

    public Date getBeginDate()
    {
        return beginDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("calDate", getCalDate())
            .append("isOpen", getIsOpen())
            .append("weekDay", getWeekDay())
            .append("beginDate", getBeginDate())
            .append("endDate", getEndDate())
            .toString();
    }
}
