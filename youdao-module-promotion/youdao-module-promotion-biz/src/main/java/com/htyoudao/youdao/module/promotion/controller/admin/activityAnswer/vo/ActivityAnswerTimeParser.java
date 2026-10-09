package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 有奖问答后台查询时间解析工具，兼容前端只传日期的日期选择器。
 */
final class ActivityAnswerTimeParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ActivityAnswerTimeParser() {
    }

    static LocalDateTime parseStart(String value) {
        return parse(value, false);
    }

    static LocalDateTime parseEnd(String value) {
        return parse(value, true);
    }

    private static LocalDateTime parse(String value, boolean endOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        if (text.indexOf('T') > 0) {
            text = text.replace('T', ' ');
        }
        if (text.length() == 10) {
            return LocalDate.parse(text, DATE_FORMATTER).atTime(endOfDay ? LocalTime.MAX.withNano(0) : LocalTime.MIN);
        }
        if (text.length() == 16) {
            return LocalDateTime.parse(text, MINUTE_FORMATTER).withSecond(endOfDay ? 59 : 0).withNano(0);
        }
        return LocalDateTime.parse(text, SECOND_FORMATTER).withNano(0);
    }
}
