package com.htyoudao.youdao.framework.common.util.date;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 时间工具类
 *
 * @author 0090
 */
public class DateUtils {

    /**
     * 时区 - 默认
     */
    public static final String TIME_ZONE_DEFAULT = "GMT+8";

    /**
     * 秒转换成毫秒
     */
    public static final long SECOND_MILLIS = 1000;

    public static final String FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";

    public static final String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss";
    public static String T_23_59_59 = " 23:59:59";
    public static String T_00_00_00 = " 00:00:00";
    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDD = "yyyyMMdd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    private static String[] SIGNALS = {"0", "1"};


    public static final String POSITIVE_SIGN = "1";

    public static final String ZERO_SIGN = "0";

    // 时间格式解析器（HH:mm:ss）
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");


    /**
     * 将 LocalDateTime 转换成 Date
     *
     * @param date LocalDateTime
     * @return LocalDateTime
     */
    public static Date of(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        // 将此日期时间与时区相结合以创建 ZonedDateTime
        ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
        // 本地时间线 LocalDateTime 到即时时间线 Instant 时间戳
        Instant instant = zonedDateTime.toInstant();
        // UTC时间(世界协调时间,UTC + 00:00)转北京(北京,UTC + 8:00)时间
        return Date.from(instant);
    }

    /**
     * 将 Date 转换成 LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime of(Date date) {
        if (date == null) {
            return null;
        }
        // 转为时间戳
        Instant instant = date.toInstant();
        // UTC时间(世界协调时间,UTC + 00:00)转北京(北京,UTC + 8:00)时间
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static Date addTime(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.toMillis());
    }

    public static boolean isExpired(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(time);
    }

    /**
     * 创建指定时间
     *
     * @param year  年
     * @param mouth 月
     * @param day   日
     * @return 指定时间
     */
    public static Date buildTime(int year, int mouth, int day) {
        return buildTime(year, mouth, day, 0, 0, 0);
    }

    /**
     * 创建指定时间
     *
     * @param year   年
     * @param mouth  月
     * @param day    日
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒
     * @return 指定时间
     */
    public static Date buildTime(int year, int mouth, int day,
                                 int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, mouth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0); // 一般情况下，都是 0 毫秒
        return calendar.getTime();
    }

    public static Date max(Date a, Date b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.compareTo(b) > 0 ? a : b;
    }

    public static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    /**
     * 是否今天
     *
     * @param date 日期
     * @return 是否
     */
    public static boolean isToday(LocalDateTime date) {
        return LocalDateTimeUtil.isSameDay(date, LocalDateTime.now());
    }

    /**
     * 是否昨天
     *
     * @param date 日期
     * @return 是否
     */
    public static boolean isYesterday(LocalDateTime date) {
        return LocalDateTimeUtil.isSameDay(date, LocalDateTime.now().minusDays(1));
    }

    public static String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, YYYYMMDD);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    public static Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String localDateToString(LocalDate date, String format) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        // 将日期转换为字符串
        return date.format(formatter);
    }

    public static LocalDateTime localDateToLocalDateTime(LocalDate date, String fix) {
        String s = localDateToString(date, FORMAT_YEAR_MONTH_DAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
        return LocalDateTime.parse(s + fix, formatter);
    }

    /**
     * 比较日期 带有延迟生效  date2 是否属于date1的有效区间内
     *
     * @param date1      日期
     * @param signal     正负号  日期前移或者后移
     * @param num        间隔日期
     * @param expiration 有效日期
     * @param date2      日期
     * @return true 小于 有效   false 大于等于 无效
     */
    public static boolean compareDateRange(LocalDateTime date1, String signal, int num, int expiration, LocalDateTime date2) {
        // signal 1 -> +   -1 -> -
        if (!ArrayUtil.contains(SIGNALS, signal)) {
            throw new RuntimeException("时间比较 符号有误");
        }
        int startInterval = 0;
        // 计算初始有效时间
        if (POSITIVE_SIGN.equals(signal)) {
            startInterval = num;
        }
        LocalDateTime start = date1;
        if (startInterval > 0) {
            start = date1.plusDays(num);
        }
        // 计算结束有效时间
        LocalDateTime end = start.plusDays(expiration);
        // 比较date2 是否属于 （start, end）
        return date2.isAfter(start) && date2.isBefore(end);
    }

    /**
     * 校验当前时间是否在营业时间段内
     *
     * @param businessHours 营业时间段列表（格式："HH:mm:ss-HH:mm:ss"）
     * @return true=营业中，false=非营业时间
     */
    public static boolean isBusinessOpen(List<String> businessHours) {
        LocalTime now = LocalTime.now();
        for (String timeRange : businessHours) {
            try {
                // 分割时间段
                String[] parts = timeRange.split("-");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid time range format: " + timeRange);
                }

                // 解析开始和结束时间
                LocalTime start = LocalTime.parse(parts[0], TIME_FORMATTER);
                LocalTime end = LocalTime.parse(parts[1], TIME_FORMATTER);

                // 处理跨天时间段（例如 23:00-01:00）
                if (end.isBefore(start)) {
                    // 跨天情况：当前时间 >= 开始时间 或 <= 结束时间
                    if (now.isAfter(start) || now.isBefore(end) || now.equals(start) || now.equals(end)) {
                        return true;
                    }
                } else {
                    // 非跨天情况：当前时间在区间内
                    if (!now.isBefore(start) && !now.isAfter(end)) {
                        return true;
                    }
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid time format in: " + timeRange, e);
            }
        }
        return false;
    }

    public static Long getSecondsToMidnight() {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 当天凌晨 1 点
        LocalDateTime target = now.withHour(1).withMinute(0).withSecond(0).withNano(0);

        // 如果已经过了凌晨 1 点，就取明天凌晨 1 点
        if (now.isAfter(target)) {
            target = target.plusDays(1);
        }

        // 计算秒数
        return ChronoUnit.SECONDS.between(now, target);
    }

    /**
     * 获取上周起止时间
     *
     * @return
     */
    public static Map<String, LocalDate> getLastWeekStartAndEnd(int n) {
        LocalDate now = LocalDate.now();

        // 当前周的周一
        LocalDate currentWeekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 前一周的起止
        LocalDate lastWeekStart = currentWeekStart.minusWeeks(n);
        LocalDate lastWeekEnd = currentWeekStart.minusDays((n-1)* 7L + 1);

        return Map.of("start", lastWeekStart, "end", lastWeekEnd);
    }


    /**
     * 获取上月起止时间
     *
     * @return
     */
    public static Map<String, LocalDate> getLastMonthStartAndEnd(Integer n) {
        LocalDate now = LocalDate.now();

        // 上个月的起止
        LocalDate lastMonth = now.minusMonths(n);
        LocalDate lastMonthStart = lastMonth.withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        return Map.of("start", lastMonthStart, "end", lastMonthEnd);
    }

    /**
     * 获取当前月份的yyyymm和上一个月的yyyymm
     *
     * @return
     */
    public static Map<String, String> getLastYYYYMMStartAndEndForWeek(LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMM");

        // 当前月份
        String currMonth = date.format(fmt);

        // 上一个月份
        String prevMonth = date.minusMonths(1).format(fmt);

        return Map.of(
                "end", currMonth,
                "start", prevMonth
        );
    }

    /**
     * 获取当前月份的yyyymm和上一个月的yyyymm
     *
     * @return
     */
    public static Map<String, String> getLastYYYYMMStartAndEndForMonth(Integer n) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMM");

        String e = now.minusMonths(n-1).format(fmt);
        String s = now.minusMonths(n).format(fmt);

        return Map.of("start", s, "end", e);
    }

    /**
     * 获取当前年份的周列表
     *
     * @return
     */
    public static List<Map<String, String>> getLastYearWeeks() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate today = LocalDate.now();

        // 本周周一
        LocalDate thisWeekMonday = today.with(DayOfWeek.MONDAY);

        // 第一个返回周：上周周一
        LocalDate weekStart = thisWeekMonday.minusWeeks(1);

        // 一年前
        LocalDate oneYearAgo = weekStart.minusYears(1);

        List<Map<String, String>> result = new ArrayList<>();

        while (!weekStart.isBefore(oneYearAgo)) {
            LocalDate weekEnd = weekStart.plusDays(6);

            Map<String, String> item = new HashMap<>();
            item.put("startDate", weekStart.format(fmt));
            item.put("endDate", weekEnd.format(fmt));
            item.put("show", weekStart.format(fmt) + "至" + weekEnd.format(fmt));

            result.add(item);

            weekStart = weekStart.minusWeeks(1);
        }

        return result;
    }

    /**
     * 获取当前年份的月列表
     *
     * @return
     */
    public static List<Map<String, String>> getLastYearMonths() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate today = LocalDate.now();

        // 本月第一天
        LocalDate thisMonthFirstDay = today.withDayOfMonth(1);

        // 第一个返回的月份：上个月
        LocalDate monthStart = thisMonthFirstDay.minusMonths(1);

        // 一年前的月份
        LocalDate oneYearAgoMonth = monthStart.minusYears(1);

        List<Map<String, String>> result = new ArrayList<>();

        while (!monthStart.isBefore(oneYearAgoMonth)) {
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            Map<String, String> map = new HashMap<>();
            map.put("startDate", monthStart.format(fmt));                 // 月初
            map.put("endDate", monthEnd.format(fmt));                   // 月末
            map.put("show", monthStart.format(DateTimeFormatter.ofPattern("yyyyMM")));

            result.add(map);

            monthStart = monthStart.minusMonths(1);
        }

        return result;
    }


}
