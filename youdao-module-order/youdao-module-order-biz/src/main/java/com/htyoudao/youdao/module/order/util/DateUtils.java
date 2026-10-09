package com.htyoudao.youdao.module.order.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具类
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";
    public static String T_00_00_00 = " 00:00:00";

    public static String T_23_59_59 = " 23:59:59";

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYYMM = "yyyyMM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYY_MM_DD_X = "yyyy/MM/dd";

    public static String YYYYMMDD = "yyyyMMdd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String HH_MM_SS = "HH:mm:ss";


    public static SimpleDateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD);

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static String getMonth() {
        return dateTimeNow(YYYY_MM);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
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

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getDatesBetweenString(String startTime, String endTime) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 解析字符串日期为 LocalDate 对象
        LocalDate startDate = LocalDate.parse(startTime, formatter);
        LocalDate endDate = LocalDate.parse(endTime, formatter);
        // 获取两个日期之间的所有日期
        List<String> datesBetween = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            datesBetween.add(currentDate.format(formatter));
            currentDate = currentDate.plusDays(1);
        }
        return datesBetween;
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }


    public static Integer trunToSubCurrMinute(String dateStr, String format) {
        try {
            Date tagert = new SimpleDateFormat(format).parse(dateStr);
            long currentTime = new Date().getTime();
            long diff = tagert.getTime() - currentTime;
            System.out.println((diff / 60000));
            return (int) (diff / 60000);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     *
     * @param endDate   最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }


    public static String localDateToString(LocalDate date, String format) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        // 将日期转换为字符串
        return date.format(formatter);
    }

    public static LocalDate StringToLocalDate(String date, String format) {
        // 定义日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 将字符串解析为LocalDate对象
        return LocalDate.parse(date, formatter);
    }


    public static String localDateTimeToString(LocalDateTime date, String format) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        // 将日期转换为字符串
        return date.format(formatter);
    }

    public static LocalDateTime stringToLocalDateTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(time, formatter);
    }

    public static LocalDate stringToLocalDate(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(time, formatter);
    }

    public static long getMinutesBetweenTimes(LocalDateTime time1, LocalDateTime time2) {
        // 计算两个LocalDateTime对象之间的差值
        Duration duration = Duration.between(time1, time2);
        // 获取相差的分钟数
        return duration.toMinutes();
    }

    public static List<LocalDate> getDatesBetween(String startTime, String endTime) {

        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS);

        // 解析字符串日期为 LocalDate 对象
        LocalDate startDate = LocalDate.parse(startTime, formatter);
        LocalDate endDate = LocalDate.parse(endTime, formatter);

        // 获取两个日期之间的所有日期
        List<LocalDate> datesBetween = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            datesBetween.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return datesBetween;
    }

    public static boolean compareDateWithAddDate(String firstDate, String endDate, int diffDayNumber) {
        SimpleDateFormat sdfDay = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);//格式化为年月
        try {
            Calendar begin = Calendar.getInstance();
            begin.setTime(sdfDay.parse(firstDate));
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), 1);
            begin.add(Calendar.DATE, diffDayNumber);
            Calendar end = Calendar.getInstance();
            end.setTime(sdfDay.parse(endDate));
            return begin.before(end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static LocalTime stringToLocalTime(String time) {
        // 创建DateTimeFormatter对象，指定时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 使用DateTimeFormatter将字符串转换为LocalTime对象
        return LocalTime.parse(time, formatter);
    }

    public static String getCurrMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return DateFormatUtils.format(calendar.getTime(), YYYY_MM_DD) + " 00:00:00";
    }

    public static String getCurrMonthTodayDay() {
        return DateFormatUtils.format(new Date(), YYYY_MM_DD) + " 23:59:59";
    }

    public static String getTodayStart() {
        return DateFormatUtils.format(new Date(), YYYY_MM_DD) + " 00:00:00";
    }

    public static String getTodayEnd() {
        return DateFormatUtils.format(new Date(), YYYY_MM_DD) + " 23:59:59";
    }

    /**
     * 获取指定日期当月的第一天
     *
     * @param dateStr
     * @return
     */
    public static String getFirstDayOfGivenMonth(String dateStr) {
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 0);
            return dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定日期的最后一天
     *
     * @param dateStr
     * @return
     */
    public static String getLastDayOfGivenMonth(String dateStr) {

        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            return dateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取下个月的指定日期
     *
     * @param day
     * @return
     */
    public static String getTargetDayOfNextMonth(int day) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DATE, day - 1);
        return dateFormat.format(calendar.getTime());
    }

    public static String getAfterTodayDate(int day) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +day);
        return dateFormat.format(calendar.getTime());
    }

    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = calendar.getTime();
        return firstDayOfMonth;
    }

    /**
     * LocalDateTime转为Date
     *
     * @param localDateTime
     * @return Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String dateToString(LocalDateTime date, String format) {
        // 创建一个SimpleDateFormat对象，指定日期格式
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // 使用SimpleDateFormat对象将Date对象转换为String对象
        return sdf.format(date);
    }

    public static String dateToStringDate(Date date, String format) {
        // 创建一个SimpleDateFormat对象，指定日期格式
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // 使用SimpleDateFormat对象将Date对象转换为String对象
        return sdf.format(date);
    }


    public static String dateToStringS(LocalDateTime localDateTime, String format) {


        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // 创建一个SimpleDateFormat对象，指定日期格式
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // 使用SimpleDateFormat对象将Date对象转换为String对象
        return sdf.format(date);
    }

    public static Date getBeforeMonth(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, amount);
        return calendar.getTime();
    }

    public static LocalDateTime timestampToLocalDateTime(String timestamp) {
        // 将字符串转换为long类型的时间戳
        long timeMillis = Long.parseLong(timestamp);

        // 使用Instant将时间戳转换为Instant对象
        Instant instant = Instant.ofEpochMilli(timeMillis);

        // 创建一个ZonedDateTime对象，使用系统默认时区
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        // 将ZonedDateTime转换为LocalDateTime
        return zonedDateTime.toLocalDateTime();
    }

    public static String getCreateTimeByOrderSn(String OrderSn) {
        String dateString = OrderSn.substring(2, 16);
        // 创建输入格式和输出格式的SimpleDateFormat对象
        SimpleDateFormat inputSdf = new SimpleDateFormat(YYYYMMDDHHMMSS);
        SimpleDateFormat outputSdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);

        // 解析输入字符串为Date对象
        Date date = null;
        try {
            date = inputSdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 格式化Date对象为目标字符串格式
        return outputSdf.format(date);

    }

    public static String timeToString(Time time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(time);
    }

    public static LocalDate getAfterDayDate(int day) {
        return LocalDate.now().minusDays(day);
    }

    /**
     * 获取n天前的yyyyMM
     *
     * @param day
     * @return
     */
    public static String getAfterDayDateToString(int day) {
        LocalDate localDate = LocalDate.now().minusDays(day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);
        String formattedDate = localDate.format(formatter);
        return formattedDate;
    }

    /**
     * 获取今天的yyyyMM
     *
     * @return
     */
    public static String getToDayDateToString() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);
        String formattedDate = localDate.format(formatter);
        return formattedDate;
    }

    /**
     * 获取本周日最后一秒
     */
    public static Date getLastSecondOfWeek() {
        LocalDate now = LocalDate.now();
        // 获取本周日的日期时间，时间部分默认是00:00:00
        LocalDate sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        String format = sunday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date date = dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, format + DateUtils.T_23_59_59);
        return date;
    }

    /**
     * 获取上周日最后一秒
     */

    public static Date getLastSecondOfLastWeek() {
        LocalDateTime now = LocalDateTime.now();
        // 获取本周日的日期时间，时间部分默认是00:00:00
        LocalDateTime sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        // 将时间部分设置为23:59:59
        LocalDateTime lastSunday = sunday.minusDays(7);
        LocalDateTime endOfLastSunday = lastSunday.withHour(23).withMinute(59).withSecond(59);
        return Date.from(endOfLastSunday.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime parseOrderTimeSafe(String orderSn) {
        if (orderSn == null || orderSn.length() < 17) {
            throw new IllegalArgumentException("订单号格式错误");
        }
        try {
            orderSn = orderSn.trim();
            String timeStr = orderSn.substring(3, 17);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间解析失败", e);
        }
    }

}
