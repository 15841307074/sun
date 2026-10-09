package com.htyoudao.youdao.module.member.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.POINTS_PRODUCT_NOT;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.WX_MEMBER_EXPORT_TIME_ERROR;

/**
 * @author dht
 * 时间段访问限制工具类
 */
public class AccessTimeRestrictionUtil {

    // 定义禁止访问的时间段
    private static final LocalTime MORNING_START = LocalTime.of(11, 30);
    private static final LocalTime MORNING_END = LocalTime.of(13, 30);
    private static final LocalTime EVENING_START = LocalTime.of(17, 30);
    private static final LocalTime EVENING_END = LocalTime.of(19, 30);

    // 异常消息模板
    private static final String ERROR_MSG_TEMPLATE = "当前时间 %s 不允许访问该接口，请于 %s 后再试";

    /**
     * 检查当前时间是否在禁止访问的时间段内
     * 如果在禁止时间段内，抛出AccessDeniedException异常
     */
    public static void checkAccessTime() {
        LocalTime currentTime = LocalTime.now();

        // 检查是否在上午禁止时间段
        boolean isInMorningRestriction =
                !currentTime.isBefore(MORNING_START) &&
                        !currentTime.isAfter(MORNING_END);

        // 检查是否在晚上禁止时间段
        boolean isInEveningRestriction =
                !currentTime.isBefore(EVENING_START) &&
                        !currentTime.isAfter(EVENING_END);

        if (isInMorningRestriction) {
            throw exception(WX_MEMBER_EXPORT_TIME_ERROR,formatTime(currentTime),formatTime(MORNING_END));
        }

        if (isInEveningRestriction) {
            throw exception(WX_MEMBER_EXPORT_TIME_ERROR,formatTime(currentTime),formatTime(EVENING_END));
        }
    }

    /**
     * 格式化时间显示
     */
    private static String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}