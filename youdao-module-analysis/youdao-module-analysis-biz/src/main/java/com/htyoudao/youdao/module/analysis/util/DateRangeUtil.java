package com.htyoudao.youdao.module.analysis.util;

import lombok.Value;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeUtil {

    /**
     * 统计用时间区间（以「自然日」为单位）
     *
     * 约定说明：
     * 1. start / end 都是「包含边界」（inclusive）
     *    - 即：区间表示为 [start, end]
     * 2. 所有统计逻辑基于「自然日」，与具体时分秒无关
     * 3. daysInclusive = 区间内包含的自然日天数
     *    - 计算公式：ChronoUnit.DAYS.between(start, end) + 1
     *    - 例如：2026-01-01 ～ 2026-01-07 → daysInclusive = 7
     *
     * 使用场景：
     * - 订货总值按天平均（门店日均货值）
     * - 同比 / 环比的等长区间计算
     * - 十天未进货门店的连续天数判定
     *
     * 注意事项（非常重要）：
     * - 本区间「不代表」最终传给 ES 的时间范围
     *   ES 查询时通常会转换为：
     *     [start 00:00:00.000 , end+1 00:00:00.000)（左闭右开）
     * - 当 end < start 时，daysInclusive 应为 0（表示空区间）
     */
    @Value
    public static class Range {

        /**
         * 区间开始日期（包含）
         * 示例：2026-01-01
         */
        LocalDate start;

        /**
         * 区间结束日期（包含）
         * 示例：2026-01-07
         */
        LocalDate end;

        /**
         * 区间内包含的自然日天数（包含首尾）
         * - 正常情况：>= 1
         * - 空区间：0
         */
        long daysInclusive;
    }


    /**
     * 按“统计包含当天”规则修正 endDate：end = min(endDate, today)
     */
    public static Range normalizeExcludeToday(String startDate, String endDate) {
        LocalDate s = LocalDate.parse(startDate);
        LocalDate e = LocalDate.parse(endDate);

        LocalDate today = LocalDate.now();

        // endDate 不能超过今天
        if (e.isAfter(today)) {
            e = today;
        }

        if (e.isBefore(s)) {
            // 返回一个空区间（业务上你也可以改为抛异常）
            return new Range(s, s.minusDays(1), 0);
        }

        long days = ChronoUnit.DAYS.between(s, e) + 1;
        return new Range(s, e, days);
    }


    /**
     * 环比：紧邻等长区间
     */
    public static Range momRange(Range current) {
        if (current.daysInclusive <= 0) return current;
        LocalDate prevEnd = current.start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(current.daysInclusive - 1);
        return new Range(prevStart, prevEnd, current.daysInclusive);
    }

    /**
     * 同比：去年同段
     */
    public static Range yoyRange(Range current) {
        if (current.daysInclusive <= 0) return current;
        LocalDate ys = current.start.minusYears(1);
        LocalDate ye = current.end.minusYears(1);
        return new Range(ys, ye, current.daysInclusive);
    }

    public static long toEpochMillisStart(LocalDate d) {
        return d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long toEpochMillisEndExclusive(LocalDate dInclusive) {
        return dInclusive.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

