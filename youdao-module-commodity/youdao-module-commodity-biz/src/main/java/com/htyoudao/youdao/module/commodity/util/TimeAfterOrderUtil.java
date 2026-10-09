package com.htyoudao.youdao.module.commodity.util;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.commodity.dal.dto.TimeBaseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author dht
 * 下单时 加购商品的判断
 */
@Slf4j
public class TimeAfterOrderUtil {

    /**
     * 启用分时场景的数据,设置 isUp 字段, true 为满足分时场景
     *
     * 分时上下架/分时置顶
     *
     * @param list
     */
    public static void processTimeBasedList(List<? extends TimeBaseDTO> list) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        //当前日期
        Integer currDate = Integer.parseInt(DateUtils.getDate().replace("-", ""));
        //当前日
        int day = now.get(Calendar.DAY_OF_MONTH);
        //当前周几
        int week = now.get(Calendar.DAY_OF_WEEK);
        //当前时
        int hour = now.get(Calendar.HOUR_OF_DAY);


        for (TimeBaseDTO timeBase : list) {
            // 检查日期范围
            timeBase.setIsUp(
                    isDateRangeValid(timeBase, currDate)
                            && isDayMatch(timeBase, day)
                            && isWeekMatch(timeBase, week)
                            && isTimeRangeMatch(timeBase, hour)
            );
        }
    }

    private static boolean isDateRangeValid(TimeBaseDTO timeBase, int currDate) {
        if (ObjectUtil.isEmpty(timeBase.getStartDate())) {
            return true; // 无日期范围限制
        }

        try {
            LocalDate startDate = timeBase.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = timeBase.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int begin = Integer.parseInt(startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            int end = Integer.parseInt(endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            return begin <= currDate && currDate <= end;
        } catch (Exception e) {
            log.error("日期解析失败", e);
            return false;
        }
    }

    private static boolean isDayMatch(TimeBaseDTO timeBase, int day) {
        String dayNumbers = timeBase.getDayNumbers();
        if (StringUtils.isBlank(dayNumbers)) {
            return true;
        }
        List<Integer> dayNumList = Arrays.stream(dayNumbers.split(","))
                .map(Integer::valueOf).toList();
        return dayNumList.contains(day);
    }

    private static boolean isWeekMatch(TimeBaseDTO timeBase, int week) {
        String weekNumbers = timeBase.getWeekNumbers();
        if (StringUtils.isBlank(weekNumbers)) {
            return true;
        }
        List<Integer> weekNumList = Arrays.stream(weekNumbers.split(","))
                .map(Integer::valueOf).toList();
        return weekNumList.contains(week);
    }

    private static boolean isTimeRangeMatch(TimeBaseDTO timeBase, int hour) {

        if (Objects.equals(timeBase.getIsAllDay(), 1)) {
            return true;
        }

        if (StringUtils.isBlank(timeBase.getTimeRange())) {
            return true;
        }

        return Arrays.stream(timeBase.getTimeRange().split(","))
                .anyMatch(range -> {
                    String[] parts = range.split("-");
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    return hour >= start && hour < end;
                });
    }
}
