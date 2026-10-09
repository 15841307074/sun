package com.htyoudao.youdao.module.commodity.util;

import cn.hutool.core.util.ObjectUtil;

import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Qizhongann
 * @date 2025-02-05
 * 此工具 适用于 时间范围 排序的实体类 ，用之前请先按照对象更新时间倒叙或正序自行查询（按需）
 * 适用此工具排序 需要继承 TimeBase 类 ，TimeBase 类已经拥有BaseEntity 类的所有属性
 * 继承TimeBase 类之前，请先查看所需实体类是否有 timeSharingTopping 字段 （字段说明：此字段为校验是否该对象已开启时间排序需要 1为开启）
 */


public class DateTimeSortUtil {

    public static void sortDateTime(List<? extends TimeBase> list) {
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
            if (ObjectUtil.isEmpty(timeBase.getTimeSharingTopping())) {
                return;
            }
            if (timeBase.getTimeSharingTopping() == 1) {
                timeBase.setIsUp(true);
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

                if (ObjectUtil.isNotEmpty(timeBase.getDayNumberList())&& timeBase.getIsUp()) {
                    if (timeBase.getDayNumberList().contains(day)) {
                        timeBase.setIsUp(true);
                    } else {
                        timeBase.setIsUp(false);
                    }
                }


                if (ObjectUtil.isNotEmpty(timeBase.getWeekNumberList())&& timeBase.getIsUp()) {
                    if (timeBase.getWeekNumberList().contains(week)) {
                        timeBase.setIsUp(true);
                    } else {
                        timeBase.setIsUp(false);
                    }
                }


                if (ObjectUtil.isNotEmpty(timeBase.getIsAllDay())&& timeBase.getIsUp()) {

                    if (timeBase.getIsAllDay().equals(1)) {
                        timeBase.setIsUp(true);
                    } else {
                        if (ObjectUtil.isNotEmpty(timeBase.getTimeRangeList())) {
                            for (String timeRange : timeBase.getTimeRangeList()) {
                                String[] timeRangeArr = timeRange.split("-");
                                if (hour >= Integer.parseInt(timeRangeArr[0]) && hour < Integer.parseInt(timeRangeArr[1])) {
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


    }
}
