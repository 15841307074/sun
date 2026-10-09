package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Schema(description = "管理后台 - 广告新增 Request VO")
@Data
public class AdvertisingSaveReqVO extends BusinessBaseDO implements Serializable {

    private Long id;


    @Schema(description = "广告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "广告名称不能为空")
    private String adName;

    @Schema(description = "广告位置(1.首页弹窗 2.首页顶部轮播 3.首页底部轮播 4.首页底部活动 5.点餐页弹窗 6.点餐页顶部轮播 7.点餐页分组轮播 8.结算页轮播 9.订单详情页轮播 10.订单列表轮播 11.开屏广告 12.浮窗)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition;

    @Schema(description = "展示门店(是否全门店 1是 2 否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllStores;

    @Schema(description = "选择门店列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> storeIds = new ArrayList<>();

    @Schema(description = "弹出方式(1.默认弹出方式 2.自下向上弹出)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer popStyle;

    @Schema(description = "应用范围（1按门店  2按标签）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicationScope;


    @Schema(description = "广告图片列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvertisingImageDO> imageDOList = new ArrayList<>();

    @Schema(description = "展示周期（1.长期展示 2.指定日期展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllTime;


    @Schema(description = "展示开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date startTime;

    @Schema(description = "展示结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date endTime;



    @Schema(description = "指定日期逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayNumbers;


    @Schema(description = "指定周几逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weekNumbers;

    @Schema(description = "标签列表逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String tagNumbers;


    @Schema(description = "指定标签列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> tagList;


    @Schema(description = "指定日期列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> dayNumberList;

    @Schema(description = "指定周几列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> weekNumberList;

    @Schema(description = "展示时段（1.全天展示 2.指定时间段展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer showTimeType;

    @Schema(description = "指定时间段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TimeVO> timeVOList;

    @Schema(description = "推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer triggerCondition;

    @Schema(description = "规则（1.周期内一次 2.周期内每天一次 3.每次进入）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ruler;

    @Schema(description = "类型（1.首页 2.点餐 3.订单及结算 4.其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionType;

    @Schema(description = "广告排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer advertisingSort;


    private List<Integer> floatingWindowDisplay = new ArrayList<>(); //浮窗显示页面 （0.全部  1.首页 2.订单结算页 3.点餐页 4.订单详情页 5.积分商城页 6.个人中心页）

    @Schema(description = "浮窗是否全选 (1.是 2.否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer floatingIsAll;

    @Schema(description = "时长 不能超过 5s", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @Schema(description = "人群列表集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> crowdIds;

//    @Schema(description = "人群字段", requiredMode = Schema.RequiredMode.REQUIRED)
//    private String crowds;

    @Schema(description = "排列方式（1 横栏展示  2双栏展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer arrangeMethod;


    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if(ObjectUtil.isNotEmpty(dayNumberList)){
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.dayNumbers = null;
        }
    }
    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if(ObjectUtil.isNotEmpty(weekNumberList)){
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.weekNumbers = null;
        }
    }

//    public void setCrowdIds(List<Long> crowdIds) {
//        this.crowdIds = crowdIds;
//        if(ObjectUtil.isNotEmpty(crowdIds)){
//            this.crowds = crowdIds.toString().replace("[", "").replace("]", "").replace(" ","");
//        }else {
//            this.crowds = null;
//        }
//    }

//    public void setCrowds(String crowds) {
//        this.crowds = crowds;
//        if(ObjectUtil.isNotEmpty(crowds)){
//            String[] array = crowds.split(",");
//            this.crowdIds = new ArrayList<>();
//            for (int i = 0; i < array.length; i++) {
//                this.crowdIds.add(Long.parseLong(array[i]));
//            }
//        }else {
//            this.crowdIds = null;
//        }
//    }


    public void setTagList(List<Long> tagList) {
        this.tagList = tagList;
        if(ObjectUtil.isNotEmpty(tagList)){
            this.tagNumbers = tagList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.tagNumbers = null;
        }
    }
    public void setTagNumbers(String tagNumbers) {
        this.tagNumbers = tagNumbers;
        if(ObjectUtil.isNotEmpty(tagNumbers)){
            String[] array = tagNumbers.split(",");
            this.tagList = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.tagList.add(Long.parseLong(array[i]));
            }
        }else {
            this.tagList = null;
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
