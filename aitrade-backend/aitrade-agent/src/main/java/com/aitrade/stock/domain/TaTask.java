package com.aitrade.stock.domain;

import java.util.Date;

import com.aitrade.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.aitrade.common.annotation.Excel;

/**
 * 任务规划对象 ta_task
 * 
 * @author aitrade
 * @date 2026-10-11
 */
public class TaTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 任务启动起始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "任务启动起始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startBeginTime;

    /** 任务启动截止时间，一个任务如果截止时间已经过期，则不再被启动 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "任务启动截止时间，一个任务如果截止时间已经过期，则不再被启动", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startEndTime;

    /** 任务完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "任务完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 任务状态0未启动1正在进行2已经完成 */
    @Excel(name = "任务状态0未启动1正在进行2已经完成")
    private Integer status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setStartBeginTime(Date startBeginTime) 
    {
        this.startBeginTime = startBeginTime;
    }

    public Date getStartBeginTime() 
    {
        return startBeginTime;
    }

    public void setStartEndTime(Date startEndTime) 
    {
        this.startEndTime = startEndTime;
    }

    public Date getStartEndTime() 
    {
        return startEndTime;
    }

    public void setFinishTime(Date finishTime) 
    {
        this.finishTime = finishTime;
    }

    public Date getFinishTime() 
    {
        return finishTime;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("startBeginTime", getStartBeginTime())
            .append("startEndTime", getStartEndTime())
            .append("finishTime", getFinishTime())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
