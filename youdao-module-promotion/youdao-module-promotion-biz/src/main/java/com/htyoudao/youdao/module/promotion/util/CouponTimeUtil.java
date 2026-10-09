package com.htyoudao.youdao.module.promotion.util;

import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.TIME_SLOT_ERROR;

/**
 * @author dht
 * 优惠券生成领取有效时间 抛异常
 */
public class CouponTimeUtil {

    private static final String[] CHINESE_WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    /**
     * 根据时效判断开始和过期时间 版本2
     * @param goodCoupon goodCoupon
     * @param userCoupon userCoupon
     */
    public static void parseCouponTime(GoodCouponRespVO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_0)) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
        //立即生效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_1)) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_2)) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //指定周几失效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_3)) {
            String useTime = goodCoupon.getUseTime();
            int day = Integer.parseInt(useTime);
            LocalDate now = LocalDate.now();
            // 获取本周日的日期时间，时间部分默认是00:00:00
            DayOfWeek dayOfWeek = DayOfWeek.of(day);
            LocalDate sunday = now.with(TemporalAdjusters.nextOrSame(dayOfWeek));
            String format = sunday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            goodCoupon.setCouponStartTime(new Date());
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, format + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, format + DateUtils.T_23_59_59));
        }
    }


    /**
     * 根据时效判断开始和过期时间 版本1
     * @param goodCoupon goodCoupon
     * @param userCoupon userCoupon
     */
    public static void parseCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_0)) {
            String[] split = goodCoupon.getUseTime().split("#");
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
        //立即生效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_1)) {
            goodCoupon.setCouponStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));

        }
        //领券后N天生效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_2)) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1), DateUtils.YYYY_MM_DD);
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //指定周几失效
        if (Objects.equals(goodCoupon.getUseType(), GoodCouponConstants.USE_TYPE_3)) {
            String useTime = goodCoupon.getUseTime();
            int day = Integer.parseInt(useTime);
            LocalDate now = LocalDate.now();
            // 获取本周日的日期时间，时间部分默认是00:00:00
            DayOfWeek dayOfWeek = DayOfWeek.of(day);
            LocalDate sunday = now.with(TemporalAdjusters.nextOrSame(dayOfWeek));
            String format = sunday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            goodCoupon.setCouponStartTime(new Date());
            goodCoupon.setCouponEndTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, format + DateUtils.T_23_59_59));
            userCoupon.setVaildStartTime(new Date());
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, format + DateUtils.T_23_59_59));
        }
    }


    /**
     * 验证优惠券是否在有效领取时间内
     *
     * @param coupon 优惠券对象
     */
    public static void validateCouponTime(GoodCouponRespVO coupon) {
        // 不限制时间直接返回true
        if (coupon.getClaimTimeLimit() == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 限制类型1: 多条件组合验证
        if (coupon.getClaimTimeLimit() == 1) {
            if (validateTimeSlot(coupon.getClaimTimeSlot(), now) && validateDayNo(coupon.getClaimDayNo(), now) && validateWeekNo(coupon.getClaimWeekNo(), now)) {
                validateTimeRange(coupon.getClaimTime(), now);
            }
        }
    }

    /**
     * 验证优惠券包是否在有效领取时间内
     *
     * @param coupon 优惠券对象
     */
    public static void validateCouponTime(CouponPackageDO coupon) {
        // 不限制时间直接返回true
        if (coupon.getClaimTimeLimit() == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 限制类型1: 多条件组合验证
        if (coupon.getClaimTimeLimit() == 1) {
            if (validateTimeSlot(coupon.getClaimTimeSlot(), now) && validateDayNo(coupon.getClaimDayNo(), now) && validateWeekNo(coupon.getClaimWeekNo(), now)) {
                validateTimeRange(coupon.getClaimTime(), now);
            }
        }
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


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(dates[0], formatter);
        LocalDate endDate = LocalDate.parse(dates[1], formatter);
        LocalDate currentDate = now.toLocalDate();
        boolean b = !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
        if (!b) {
            String message = startDate + "至"+ endDate +"日期内有效";
            throw exception(TIME_SLOT_ERROR,message);
        }
        return true;

    }

    /**
     * 验证指定几号 (格式: 1,2,6)
     */
    private static boolean validateDayNo(String claimDayNo, LocalDateTime now) {
        // 没设置视为不限制
        if (claimDayNo == null || claimDayNo.isEmpty()) {
            return true;
        }

        List<Integer> validDays = Arrays.stream(claimDayNo.split("#"))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        boolean contains = validDays.contains(now.getDayOfMonth());
        if(!contains){
            String message = claimDayNo +"日期内有效";
            throw exception(TIME_SLOT_ERROR,message);
        }
        return true;

    }

    /**
     * 验证指定周几 (格式: 1#3 表示周一、周三)
     */
    private static boolean validateWeekNo(String claimWeekNo, LocalDateTime now) {
        // 没设置视为不限制
        if (claimWeekNo == null || claimWeekNo.isEmpty()) {
            return true;
        }


        List<DayOfWeek> validWeeks = Arrays.stream(claimWeekNo.split("#"))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(DayOfWeek::of)
                .toList();
        boolean contains = validWeeks.contains(now.getDayOfWeek());
        if(!contains){
            String[] numbers = claimWeekNo.split("#");
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < numbers.length; i++) {
                int num = Integer.parseInt(numbers[i].trim());
                if (num >= 1 && num <= 7) {
                    if (i > 0) {
                        result.append("、");
                    }
                    result.append(CHINESE_WEEKDAYS[num]);
                }
            }
            String message = result.append("可领取").toString();
            throw exception(TIME_SLOT_ERROR,message);
        }
        return true;
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


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime startTime = LocalTime.parse(times[0], formatter);
        LocalTime endTime = LocalTime.parse(times[1], formatter);
        LocalTime currentTime = now.toLocalTime();
        boolean b = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        if(!b){
            String message = startTime + "-"+ endTime +"时间段内有效";
            throw exception(TIME_SLOT_ERROR,message);
        }
        return true;

    }
}
