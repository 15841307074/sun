package com.htyoudao.youdao.module.promotion.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.*;

/**
 * 时间判断工具类
 */
public class TimeValidationUtil {

    /**
     * 判断当前时间是否满足所有时间条件（字符串参数版）
     */
    public static boolean isTimeValid(Date startDate,
                                      Date endDate,
                                      String dayNumbers,
                                      String weekNumbers,
                                      String timeRanges) {
        // 转换参数类型后调用核心方法
        return isTimeValidCore(
                startDate,
                endDate,
                parseNumberList(dayNumbers),
                parseNumberList(weekNumbers),
                timeRanges != null ? Arrays.asList(timeRanges.split(",")) : null
        );
    }

    public static void main(String[] args) {
        LocalDateTime dateTime = LocalDate.now().atStartOfDay();
        Date from = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

        List<String> strings = List.of("2-18","12:30-14:30", "13:00-15:30", "14-22", "4-23", "23-24", "4-23","11:00-12:00,13:00-18:00","17:30-18:00");
        for (String string : strings) {
            System.out.println(string +  isTimeValid(from, from, "", "", string));
        }
    }


    /**
     * 判断当前时间是否满足所有时间条件（List参数版）
     */
    public static boolean isTimeValid(Date startDate,
                                      Date endDate,
                                      List<Integer> dayList,
                                      List<Integer> weekList,
                                      List<String> timeRangeList) {
        return isTimeValidCore(
                startDate,
                endDate,
                dayList,
                weekList,
                timeRangeList
        );
    }

    /**
     * 核心判断逻辑（私有方法）
     */
    private static boolean isTimeValidCore(Date startDate,
                                           Date endDate,
                                           List<Integer> dayList,
                                           List<Integer> weekList,
                                           List<String> timeRangeList) {
        Calendar now = Calendar.getInstance();

        // 1. 日期范围检查
        if (!checkDateRange(startDate, endDate)) {
            return false;
        }

        // 2. 具体日期检查
        if (!checkDayOfMonth(now, dayList)) {
            return false;
        }

        // 3. 星期检查
        if (!checkDayOfWeek(now, weekList)) {
            return false;
        }

        // 4. 时间段检查
        return checkTimeRanges(now, timeRangeList);
    }

    /**
     * 检查日期范围
     */
    private static boolean checkDateRange(Date startDate, Date endDate) {
        // 获取今天开始的时间戳
        long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();


        // 开始日期检查：今天是否早于开始日期
        if (startDate != null && todayStart < startDate.getTime()) {
            return false;
        }

        // 结束日期检查：今天是否晚于结束日期
        if (endDate != null && todayStart > endDate.getTime()) {
            return false;
        }

        return true;
    }

    /**
     * 检查具体日期
     */
    private static boolean checkDayOfMonth(Calendar now, List<Integer> dayList) {
        if (dayList == null || dayList.isEmpty()) {
            return true;
        }
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        return dayList.contains(currentDay);
    }

    /**
     * 检查星期几
     */
    private static boolean checkDayOfWeek(Calendar now, List<Integer> weekList) {
        if (weekList == null || weekList.isEmpty()) {
            return true;
        }
        int currentWeek = now.get(Calendar.DAY_OF_WEEK);
        return weekList.contains(currentWeek);
    }

    /**
     * 检查时间段，支持小时级别（如8-12）和分钟级别（如2:00-22:00）的格式
     */
    private static boolean checkTimeRanges(Calendar now, List<String> timeRangeList) {

              // 添加空值检查
        if (timeRangeList == null || timeRangeList.isEmpty()) {
            return true;
        }

        try {
            // 统一处理时间格式
            List<String> list = timeRangeList.stream().map(TimeValidationUtil::normalizeTimeRange).toList();
            LocalTime currentTime = LocalTime.now();

            for (String s : list) {
                LocalTime startTime = parseTime(s.split("-")[0]);
                LocalTime endTime = parseTime(s.split("-")[1]);
                if (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)) {
                    return true;
                }

            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析时间字符串
     */
    private static LocalTime parseTime(String timeStr) {
        if (timeStr.equals("24") || timeStr.equals("24:00")){
            return LocalTime.of(23,59,59);
        }
        if (timeStr.length() <= 2) {
            // 只有小时，如 "14"
            return LocalTime.of(Integer.parseInt(timeStr), 0, 0);
        } else {
            // 标准格式，如 "14:30"
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute,0);
        }
    }


    /**
     * 统一时间范围格式
     */
    private static String normalizeTimeRange(String timeRange) {
        String cleaned = timeRange.replaceAll("\\s", "");

        // 处理各种格式
        if (cleaned.matches("\\d{1,2}-\\d{1,2}")) {
            // 格式：12-14 → 12:00-14:00
            String[] parts = cleaned.split("-");
            return parts[0] + ":00-" + parts[1] + ":00";
        } else if (cleaned.matches("\\d{1,2}:\\d{2}-\\d{1,2}")) {
            // 格式：12:30-14 → 12:30-14:00
            String[] parts = cleaned.split("-");
            return parts[0] + "-" + parts[1] + ":00";
        } else if (cleaned.matches("\\d{1,2}-\\d{1,2}:\\d{2}")) {
            // 格式：12-14:30 → 12:00-14:30
            String[] parts = cleaned.split("-");
            return parts[0] + ":00-" + parts[1];
        }

        return cleaned; // 已经是标准格式
    }

    /**
     * 将时间字符串解析为当天的总分钟数
     * 支持两种格式：
     * 1. 仅小时：如 "8" 表示 8:00
     * 2. 小时:分钟：如 "8:30" 表示 8时30分
     */
    private static int parseTimeToMinutes(String timeStr) {
        String[] timeParts = timeStr.split(":");

        int hour, minute = 0;

        // 解析小时部分
        hour = Integer.parseInt(timeParts[0]);
        if (hour < 0 || hour >= 24) {
            throw new IllegalArgumentException("无效的小时值: " + hour);
        }

        // 如果有分钟部分则解析
        if (timeParts.length == 2) {
            minute = Integer.parseInt(timeParts[1]);
            if (minute < 0 || minute >= 60) {
                throw new IllegalArgumentException("无效的分钟值: " + minute);
            }
        } else if (timeParts.length > 2) {
            throw new IllegalArgumentException("无效的时间格式: " + timeStr);
        }

        return hour * 60 + minute;
    }


    /**
     * 解析逗号分隔的数字字符串为List<Integer>
     */
    private static List<Integer> parseNumberList(String numbers) {
        if (numbers == null || numbers.isBlank()) {
            return null;
        }
        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 简化版判断 - 仅判断当前时间是否在时间段内（字符串参数版）
     */
    private static boolean isInTimeRanges(String timeRanges) {
        List<String> rangeList = timeRanges != null ?
                Arrays.asList(timeRanges.split(",")) : null;
        return isInTimeRanges(rangeList);
    }

    /**
     * 简化版判断 - 仅判断当前时间是否在时间段内（List参数版）
     */
    private static boolean isInTimeRanges(List<String> timeRangeList) {
        Calendar now = Calendar.getInstance();
        return checkTimeRanges(now, timeRangeList);
    }
}