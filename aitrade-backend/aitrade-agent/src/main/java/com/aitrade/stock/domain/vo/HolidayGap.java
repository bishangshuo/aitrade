package com.aitrade.stock.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class HolidayGap implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean willBeHoliday; // 是否将要出现中假期
    private Date nowDate;//当前日期
    private Date beginDateOfHoliday; //出现假期的起始日
    private Date tradeDateAfterHoliday; //假期后的第一个交易日
    private Integer holidayLong; //假期长度
    private Integer comingDistance; //还有多少天遇上假期
}
