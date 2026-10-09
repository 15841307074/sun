package com.htyoudao.youdao.module.order.util;

import cn.hutool.core.util.ObjectUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class SubTableUtil {

    public static final String BENGIN_MONTH = "2024-04";

    /**
     * 根据开始时间和结束时间查询
     * 返回时间段内的表集合
     * 入参 Date类型
     */
    public static List<String> getTableListByDateRange(String tableName, Date startDate, Date endDate) {
//        if(ObjectUtil.isEmpty(tableName)){
//            return new ArrayList<>();
//        }
        String startDateStr = "";
        String endDateStr = "";
        if (ObjectUtil.isEmpty(startDate)) {
            startDateStr = BENGIN_MONTH;
        } else {
            startDateStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM, startDate);
        }
        if (ObjectUtil.isEmpty(endDate)) {
            endDateStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date());
        } else {
            endDateStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM, endDate);
        }
        return getTableListByRange(tableName, startDateStr, endDateStr);
    }


    /**
     * 根据开始时间和结束时间查询
     * 返回时间段内的表集合
     * 入参 String 类型
     */
    public static List<String> getTableListByStringRange(String tableName, String startDate, String endDate) {
//        if (ObjectUtil.isEmpty(tableName)) {
//            return new ArrayList<>();
//        }
        String startDateStr = startDate;
        String endDateStr = endDate;
        if (ObjectUtil.isEmpty(startDate)) {
            startDateStr = BENGIN_MONTH;
        }
        if (ObjectUtil.isEmpty(endDate)) {
            endDateStr = DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date());
        }
        return getTableListByRange(tableName, startDateStr, endDateStr);
    }

    public static void main(String[] args) {
        System.out.println(getTableListByStringRange("", "2023-01-01", "2025-02-04"));

    }

    /**
     * 根据返回的时间集合 拼接为表名返回
     */
    public static List<String> getTableListByRange(String tableName, String startDate, String endDate) {


        List<String> monthList = getMonthBetween(startDate, endDate);
        List<String> tableList = new ArrayList<>();
        monthList.stream().forEach(e -> {
            tableList.add(tableName + "_" + e);
        });
        return tableList;
    }

    /**
     * 增加 获取两个日期之间的月份
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();
        SimpleDateFormat sdfDay = new SimpleDateFormat(DateUtils.YYYY_MM_DD);//格式化为年月
        SimpleDateFormat sdfMonth = new SimpleDateFormat(DateUtils.YYYYMM);//格式化为年月

        try {
            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.setTime(sdfDay.parse(SubTableUtil.BENGIN_MONTH + "-01"));
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), 1);
            end.setTime(new Date());
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), 1);
            end.add(Calendar.MONTH, 1);
            end.add(Calendar.DATE, -1);
            min.setTime(sdfDay.parse(minDate + "-01"));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
            max.setTime(sdfDay.parse(maxDate + "-01"));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
            Calendar curr = min;
            while (curr.before(max)) {
                if (!curr.before(begin) && !curr.after(end)) {
                    result.add(sdfMonth.format(curr.getTime()));
                }
                curr.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 根据单个时间 查询出对应的表名
     * 入参Date类型
     */
    public static String getTableListByDateSingle(String tableName, Date createDate) {
        if (ObjectUtil.isEmpty(createDate)) {
            return tableName + "_" + DateUtils.parseDateToStr(DateUtils.YYYYMM, new Date());
        } else {
            return tableName + "_" + DateUtils.parseDateToStr(DateUtils.YYYYMM, createDate);
        }
    }

    /**
     * 根据单个时间 查询出对应的表名
     * 入参 String类型
     */
    public static String getTableListByStringSingle(String tableName, String createDate) {
        if (ObjectUtil.isEmpty(createDate)) {
            return tableName + "_" + createDate.substring(0, createDate.lastIndexOf("-"));
        } else {
            return tableName + "_" + DateUtils.parseDateToStr(DateUtils.YYYYMM, new Date());
        }
    }

    /**
     * 获取某个表所有的表名 逆序排列
     * 从时间最近的表查询提高查询效率
     */
    public static List<String> getAllTableList(String tableName) {
        List<String> tableListByStringRange = getTableListByStringRange(tableName, BENGIN_MONTH, DateUtils.getMonth());
        Collections.reverse(tableListByStringRange);
        return tableListByStringRange;
    }

}
