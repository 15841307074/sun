package com.htyoudao.youdao.module.promotion.util;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/**
 * @author dht
 */
public class TimeRangeChecker {

    /**
     * 检查时间 定时发送短信 8-10 14-16之间
     * @param dateTime dateTime
     */
    public static void timeCheck(LocalDateTime dateTime) {
        // 获取传入时间的时间部分
        LocalTime time = dateTime.toLocalTime();

        // 定义时间范围（闭区间）
        LocalTime morningStart = LocalTime.of(8, 0);
        LocalTime morningEnd = LocalTime.of(10, 0);
        LocalTime afternoonStart = LocalTime.of(14, 0);
        LocalTime afternoonEnd = LocalTime.of(16, 0);

        // 检查时间是否在任一范围内（包含边界）
        boolean isInRange =
                (!time.isBefore(morningStart) && !time.isAfter(morningEnd)) ||
                (!time.isBefore(afternoonStart) && !time.isAfter(afternoonEnd));

        if (!isInRange) {
            throw exception(SET_TIME_ERROR);
        }

        // 检查是否是未来时间
        boolean isFuture = dateTime.isAfter(LocalDateTime.now());
        if (!isFuture) {
            throw exception(LAST_TIME_ERROR);
        }
    }

    /**
     * 检查时间 定时发送短信 8-10 14-16之间
     * @param dateTime dateTime
     */
    public static void timeCheckScheduled(LocalDateTime dateTime) {
        // 获取传入时间的时间部分
        LocalTime time = dateTime.toLocalTime();

        // 定义时间范围（闭区间）
        LocalTime morningStart = LocalTime.of(8, 0);
        LocalTime morningEnd = LocalTime.of(10, 0);
        LocalTime afternoonStart = LocalTime.of(14, 0);
        LocalTime afternoonEnd = LocalTime.of(16, 0);

        // 检查时间是否在任一范围内（包含边界）
        boolean isInRange =
                (!time.isBefore(morningStart) && !time.isAfter(morningEnd)) ||
                        (!time.isBefore(afternoonStart) && !time.isAfter(afternoonEnd));

        if (!isInRange) {
            throw exception(SET_TIME_ERROR);
        }
    }
}