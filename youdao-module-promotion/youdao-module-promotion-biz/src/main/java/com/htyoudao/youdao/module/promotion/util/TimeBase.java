package com.htyoudao.youdao.module.promotion.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeBase extends BusinessBaseDO {


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



    @TableField(exist = false)
    private List<Integer> dayNumberList;
    @TableField(exist = false)
    private List<Integer> weekNumberList;

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

}
