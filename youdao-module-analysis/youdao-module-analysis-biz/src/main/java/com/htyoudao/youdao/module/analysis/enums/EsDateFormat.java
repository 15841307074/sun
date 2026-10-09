package com.htyoudao.youdao.module.analysis.enums;

import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EsDateFormat {
    HOUR("hour", "yyyy-MM-dd HH", CalendarInterval.Hour),
    DAY("day", "yyyy-MM-dd", CalendarInterval.Day),
;

    private final String value;
    private final String format;
    private final CalendarInterval calendarInterval;
}
