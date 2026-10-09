package com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 活动详情 Response VO")
@Data
public class ActivityMjInfoRespVO {

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




    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityName;

    /**
     * 优惠类型 （1满N元，2满N件）
     */
    @Schema(description = "优惠类型 （1满N元，2满N件）")
    private Integer discountType;



    /**
     * 优惠折扣（1满xx元减xx元 2满xx元减xx折）
     */
    @Schema(description = "优惠折扣（1满xx元减xx元 2满xx元减xx折）")
    private Integer discountOffer;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    private Integer discountRules;

    /**
     * 优惠设置
     */
    @Schema(description = "优惠设置")
    private String discountSettings;



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

    @Schema(description = "存储活动标识，如1-优惠券")
    private List<Integer> stackableActivitieList;

    private List<Integer> dayNumberList = new ArrayList<>();

    private List<Integer> weekNumberList = new ArrayList<>();

    private List<String> timeRangeList = new ArrayList<>();

    @Schema(description = "设置集合")
    private List<String> settingList = new ArrayList<>();

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


    public void setSettingList(List<String> settingList) {
        this.settingList = settingList;
        if(ObjectUtil.isNotEmpty(settingList)){
            this.discountSettings = settingList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.discountSettings = "";
        }
    }


    public void setDiscountSettings(String discountSettings) {
        this.discountSettings = discountSettings;
        if(ObjectUtil.isNotEmpty(discountSettings)){
            String[] array = discountSettings.split(",");
            this.settingList = new ArrayList<>();
            Collections.addAll(this.settingList, array);
        }else {
            this.settingList = null;
        }
    }



}
