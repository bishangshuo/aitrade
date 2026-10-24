package com.aitrade.common.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

public final class DateConvertUtils {

    /** A股以北京时间（Asia/Shanghai）为准，避免服务器时区不同导致日期偏移 */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private DateConvertUtils() {}

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZONE).toLocalDate();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZONE).toInstant());
    }

    public static YearMonth toYearMonth(Date date) {
        LocalDate ld = toLocalDate(date);
        return ld == null ? null : YearMonth.from(ld);
    }
}