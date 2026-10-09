package com.htyoudao.youdao.module.commodity.util;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Qizhongann
 * @date 2025-02-05
 * 此工具 适用于 时间范围 排序的实体类 ，用之前请先按照对象更新时间倒叙或正序自行查询（按需）
 * 适用此工具排序 需要继承 TimeBase 类 ，TimeBase 类已经拥有BaseEntity 类的所有属性
 * 继承TimeBase 类之前，请先查看所需实体类是否有 timeSharingTopping 字段 （字段说明：此字段为校验是否该对象已开启时间排序需要 1为开启）
 */


public class DateTimeStatusUtil {

    public static void statusDateTime(List<? extends TimeBase> list) {
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

        for (TimeBase timeBase : list) {
            timeBase.setIsUp(false);
            if (StringUtils.isNotEmpty(timeBase.getDayNumbers())) {
                if (timeBase.getDayNumbers().contains(",")) {
                    String[] array = timeBase.getDayNumbers().split(",");
                    for (int i = 0; i < array.length; i++) {
                        timeBase.setDayNumberList(Arrays.stream(array)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()));
                    }
                } else {
                    List<Integer> dayNumberList = new ArrayList<>();
                    dayNumberList.add(Integer.parseInt(timeBase.getDayNumbers()));
                    timeBase.setDayNumberList(dayNumberList) ;
                }
            }
            if (StringUtils.isNotEmpty(timeBase.getWeekNumbers())) {
                if (timeBase.getWeekNumbers().contains(",")) {
                    String[] array = timeBase.getWeekNumbers().split(",");
                    for (int i = 0; i < array.length; i++) {
                        timeBase.setWeekNumberList( Arrays.stream(array)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()));
                    }
                } else {
                    List<Integer> weekNumberList = new ArrayList<>();
                    weekNumberList.add(Integer.parseInt(timeBase.getWeekNumbers()));
                    timeBase.setWeekNumberList(weekNumberList) ;
                }
            }
            if (StringUtils.isNotEmpty(timeBase.getTimeRange())) {
                if (timeBase.getTimeRange().contains(",")) {
                    String[] array = timeBase.getTimeRange().split(",");
                    for (int i = 0; i < array.length; i++) {
                        timeBase.setTimeRangeList(Arrays.stream(array)
                                .collect(Collectors.toList()));
                    }
                } else {
                    List<String> timeRangeList = new ArrayList<>();
                    timeRangeList.add(timeBase.getTimeRange());
                    timeBase.setTimeRangeList(timeRangeList) ;
                }
            }
            if (!ObjectUtil.isEmpty(timeBase.getIsAllDay()) && timeBase.getIsAllDay() == 1) {
                timeBase.setIsUp(true);
                continue;
            }

            if (ObjectUtil.isNotEmpty(timeBase.getStartDate())) {
                Integer begin = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, timeBase.getStartDate()).replace("-", ""));
                //结束日期
                Integer end = Integer.parseInt(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, timeBase.getEndDate()).replace("-", ""));
                //不显示

                if (begin <= currDate && currDate <= end) {
                    //在日期范围内
                    timeBase.setIsUp(true);
                } else {
                    timeBase.setIsUp(false);
                }
            }

            if (!timeBase.getIsUp()) {
                if (ObjectUtil.isNotEmpty(timeBase.getDayNumberList())) {
                    if (timeBase.getDayNumberList().contains(day)) {
                        timeBase.setIsUp(true);
                    } else {
                        timeBase.setIsUp(false);
                    }
                }
            }


            if (!timeBase.getIsUp()) {
                if (ObjectUtil.isNotEmpty(timeBase.getWeekNumberList())) {
                    if (timeBase.getWeekNumberList().contains(week)) {
                        timeBase.setIsUp(true);
                    } else {
                        timeBase.setIsUp(false);
                    }
                }
            }


            if (!timeBase.getIsUp()) {
                if (ObjectUtil.isNotEmpty(timeBase.getIsAllDay())) {
                    if (timeBase.getIsAllDay().equals(1)) {
                        timeBase.setIsUp(true);
                    } else {
                        if (ObjectUtil.isNotEmpty(timeBase.getTimeRangeList())) {
                            for (String timeRange : timeBase.getTimeRangeList()) {
                                String[] timeRangeArr = timeRange.split("-");
                                if (hour >= Integer.parseInt(timeRangeArr[0]) && hour <= Integer.parseInt(timeRangeArr[1])) {
                                    timeBase.setIsUp(true);
                                    break;
                                } else {
                                    timeBase.setIsUp(false);
                                }
                            }
                        } else {
                            timeBase.setIsUp(false);
                        }
                    }
                }
            }
        }
        if (list.size() > 1) {

            Collections.sort(list, new Comparator<TimeBase>() {
                @Override
                public int compare(TimeBase o1, TimeBase o2) {
                    return o1.getIsUp().compareTo(o2.getIsUp());
                }
            });
        }
    }
}
