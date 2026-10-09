package com.htyoudao.youdao.module.commodity.dal.dataobject;

import cn.hutool.core.util.ObjectUtil;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = true)
public class TimeBase  extends BusinessBaseDO {

    /**
     * 是否开启分时置顶 1是 0否
     */
    private Integer timeSharingTopping;



    /**
     * 是否置顶
     */
    @TableField(exist = false)
    private Boolean isUp;


    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date endDate;

    /**
     * 指定日期逗号分割
     */
    private String dayNumbers;

    /**
     * 指定周几逗号分割
     */
    private String weekNumbers;
    /**
     * 是否全天时段 1是 0否
     */
    private Integer isAllDay;

    /**
     * 指定时间段
     */
    private String timeRange;


    @TableField(exist = false)
    private List<Integer> dayNumberList;
    @TableField(exist = false)
    private List<Integer> weekNumberList;
    @TableField(exist = false)
    private List<String> timeRangeList;

    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if(ObjectUtil.isNotEmpty(dayNumberList)){
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.dayNumbers = "";
        }
    }
    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if(ObjectUtil.isNotEmpty(weekNumberList)){
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.weekNumbers = "";
        }
    }
    public void setTimeRangeList(List<String> timeRangeList) {
        this.timeRangeList = timeRangeList;
        if(ObjectUtil.isNotEmpty(timeRangeList)){
            this.timeRange = timeRangeList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.timeRange = "";
        }
    }
    public void setDayNumbers(String dayNumbers) {
        this.dayNumbers = dayNumbers;
        if(ObjectUtil.isNotEmpty(dayNumbers)){
            String[] array = dayNumbers.split(",");
            this.dayNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.dayNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.dayNumberList = null;
        }
    }
    public void setWeekNumbers(String weekNumbers) {
        this.weekNumbers = weekNumbers;
        if(ObjectUtil.isNotEmpty(weekNumbers)){
            String[] array = weekNumbers.split(",");
            this.weekNumberList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.weekNumberList.add(Integer.parseInt(array[i]));
            }
        }else {
            this.weekNumbers = null;
        }
    }
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        if(ObjectUtil.isNotEmpty(timeRange)){
            String[] array = timeRange.split(",");
            this.timeRangeList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.timeRangeList.add(array[i]);
            }
        }else {
            this.timeRangeList = null;
        }
    }
}
