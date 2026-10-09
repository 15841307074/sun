package com.htyoudao.youdao.module.promotion.util;

import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @author dht
 * 优惠券定时任务判断领取时间是否有效 不抛异常
 */
public class CouponScheduledTimeUtil {

    /**
     * 验证优惠券是否在有效领取时间内
     * @param coupon 优惠券对象
     */
    public static Boolean validateCouponTime(GoodCouponDO coupon) {
        // 不限制时间直接返回true
        if (coupon.getClaimTimeLimit() == 0) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // 限制类型1: 多条件组合验证
        if (coupon.getClaimTimeLimit() == 1) {
            return validateTimeSlot(coupon.getClaimTimeSlot(), now)
                    && validateDayNo(coupon.getClaimDayNo(), now)
                    && validateWeekNo(coupon.getClaimWeekNo(), now)
                    && validateTimeRange(coupon.getClaimTime(), now);
        }
        return false;
    }


    /**
     * 验证日期段 (格式: yyyy-MM-dd#yyyy-MM-dd)
     */
    private static boolean validateTimeSlot(String timeSlot, LocalDateTime now) {
        // 没设置视为不限制
        if (timeSlot == null || timeSlot.isEmpty()) {
            return true;
        }

        String[] dates = timeSlot.split("#");
        if (dates.length != 2) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(dates[0], formatter);
            LocalDate endDate = LocalDate.parse(dates[1], formatter);
            LocalDate currentDate = now.toLocalDate();
            return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证指定几号 (格式: 1,2,6)
     */
    private static boolean validateDayNo(String claimDayNo, LocalDateTime now) {
        // 没设置视为不限制
        if (claimDayNo == null || claimDayNo.isEmpty()) {
            return true;
        }

        try {
            List<Integer> validDays = Arrays.stream(claimDayNo.split("#"))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
            return validDays.contains(now.getDayOfMonth());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证指定周几 (格式: 1#3 表示周一、周三)
     */
    private static boolean validateWeekNo(String claimWeekNo, LocalDateTime now) {
        // 没设置视为不限制
        if (claimWeekNo == null || claimWeekNo.isEmpty()) {
            return true;
        }

        try {
            List<DayOfWeek> validWeeks = Arrays.stream(claimWeekNo.split("#"))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .map(DayOfWeek::of)
                    .toList();
            return validWeeks.contains(now.getDayOfWeek());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证时间段 (格式: HH:mm:ss#HH:mm:ss)
     */
    private static boolean validateTimeRange(String claimTime, LocalDateTime now) {
        // 没设置视为不限制
        if (claimTime == null || claimTime.isEmpty()) {
            return true;
        }

        String[] times = claimTime.split("#");
        if (times.length != 2) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime startTime = LocalTime.parse(times[0], formatter);
            LocalTime endTime = LocalTime.parse(times[1], formatter);
            LocalTime currentTime = now.toLocalTime();
            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        } catch (Exception e) {
            return false;
        }
    }
}
