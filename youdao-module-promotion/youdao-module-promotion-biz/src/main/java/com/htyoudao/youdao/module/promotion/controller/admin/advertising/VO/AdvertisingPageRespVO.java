package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingtime.AdvertisingTimeDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 广告查询 Response VO")
@Data
public class AdvertisingPageRespVO {

    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    @Schema(description = "广告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adName;

    @Schema(description = "广告图片列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvertisingImageDO> imageDOList = new ArrayList<>();

    @Schema(description = "展示时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayTime ;


    @Schema(description = "系统判定是否启用(1 启用 2禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isOpen;

    @Schema(description = "广告详细位置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition; //广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.首页活动 5.点餐页弹屏 6.点餐页轮播图 7.点餐页全局轮播 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏 12 浮窗 13 订单详情弹窗 14 点餐页分组轮播）
    @Schema(description = "是否全门店 (1是 0否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllStores;

    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private String applicableStore;

    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS =new ArrayList<>();

    @Schema(description = "是否任何时间段(1长期展示 2指定日期展示)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllTime;

    @Schema(description = "展示开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date startTime ;

    @Schema(description = "展示结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date endTime;


    @Schema(description = "每月多少号展示", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> dayNumberList;

    @Schema(description = "每周几展示", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> weekNumberList;

    @Schema(description = "展示时段（1.全天展示 2.指定时间段展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer showTimeType;


    @Schema(description = "展示时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvertisingTimeDO> timeDOList;

    @Schema(description = "天", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayNumbers;


    @Schema(description = "周", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weekNumbers;

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
