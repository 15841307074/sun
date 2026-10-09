package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 活动详情 Response VO")
@Data
public class ActivityInfoRespVO {

    /**
     * 主键
     */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主表ID
     */
    private Long activityId;


    @Schema(description = "优惠类型 1（1第二件半件，2买一送一，3自定义优惠）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer discountType;


    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityName;

    /**
     * 优惠第几件
     */
    @Schema(description = "优惠第几件", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer discountItemNum;

    /**
     * 优惠打几折
     */
    @Schema(description = "优惠打几折", requiredMode = Schema.RequiredMode.REQUIRED)
    private double discountRate;



    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date startDate;


    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endDate;

    @Schema(description = "活动状态(1 未开始 2 进行中 3 已结束)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityStatus;


    @Schema(description = "应用范围 0 门店 1 标签")
    private Integer appScope;

    @Schema(description = "门店标签ID集合（应用范围=标签时）")
    private List<Long> tagIds;

    @Schema(description = "是否全门店 (1是 0否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityStore;


    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS =new ArrayList<>();

    @Schema(description = "适用商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityDTO> commodityDTOList = new ArrayList<>();

    @Schema(description = "存储活动标识，如1-优惠券")
    private List<Integer> stackableActivitieList;


    @Schema(description = "是否上架(0不开启 1开启)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isEnabled;

    @Schema(description = "是否可编辑", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isEditable =true;

    @Schema(description = "活动备注", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityRemark;


    @Schema(description = "优惠叠加（0不叠加 1叠加）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    private String stackableActivities;
    /**
     * 活动商品（1全部商品可用 2指定商品可用）
     */
    private Integer activityProduct;


    @Schema(description = "指定时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeRange;


    @Schema(description = "指定日期逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayNumbers;


    @Schema(description = "指定周几逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weekNumbers;

    @TableField(exist = false)
    private List<Integer> dayNumberList = new ArrayList<>();
    @TableField(exist = false)
    private List<Integer> weekNumberList = new ArrayList<>();
    @TableField(exist = false)
    private List<String> timeRangeList = new ArrayList<>();

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
            for (String s : array) {
                this.dayNumberList.add(Integer.parseInt(s));
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
            for (String s : array) {
                this.weekNumberList.add(Integer.parseInt(s));
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
            Collections.addAll(this.timeRangeList, array);
        }else {
            this.timeRangeList = null;
        }
    }



}
