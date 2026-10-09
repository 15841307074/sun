package com.htyoudao.youdao.module.promotion.util;


import java.time.LocalDateTime;

/**
 * 时间范围判断工具类
 * 入参：Date 类型开始日期、Date 类型结束日期 + String 时间段(HH:mm-HH:mm)
 */
public class DateTimeRangeUtil {

    /**
     * 判断当前时间是否在【日期 + 时间段】范围内
     *
     * @return true 在范围内 | false 不在/参数错误
     */
    public static boolean isNowInRange(LocalDateTime start, LocalDateTime end) {
        // 1. 非空校验
        if (start == null || end == null) {
            return false;
        }
        // 2. 结束时间不能早于开始时间
        if (end.isBefore(start)) {
            return false;
        }
        // 3. 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 4. 判断：开始时间 ≤ 当前时间 ≤ 结束时间（包含等于）
        return (now.isEqual(start) || now.isAfter(start))
                &&
                (now.isEqual(end) || now.isBefore(end));
    }
}
