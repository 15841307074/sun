package com.htyoudao.youdao.module.promotion.dal.dataobject.advertising;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage.AdvertisingImageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingtime.AdvertisingTimeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 小程序广告实体类
 */
@TableName("advertising")
@EqualsAndHashCode(callSuper = true)
@Data
public class AdvertisingDO extends BusinessBaseDO implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;


    private Integer adPosition;//广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.首页活动 5.点餐页弹屏 6.点餐页轮播图 7.点餐页全局轮播 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏 12 浮窗 13 订单详情弹窗 14 点餐页分组轮播）
    @Schema(description = "广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.首页活动 5.点餐页弹屏 6.点餐页轮播图 7.点餐页全局轮播 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏 12 浮窗 13 订单详情弹窗 14 点餐页分组轮播）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition;

    @Schema(description = "广告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    @Schema(description = "展示开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Date startTime ;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    @Schema(description = "展示结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endTime; //展示结束时间

    @Schema(description = "是否全门店 1是 2 否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllStores;

    private String adUrl; //图片地址
    private Integer isImageJump; //图片是否跳转 1是 2否
    private Integer jumpLocation; //图片跳转位置 (0.无跳转 1.优惠卷 2.活动 3.链接 4.注册有礼 5.入群领劵 6.积分商城 7.会员权益 8商品 9优惠劵包)
    private String jumpUrl; //图片跳转链接
    private Long jumpLocationId; // 如果跳转是优惠卷 就是优惠卷 id 如果是活动 就是活动id

    @Schema(description = "时长 不能超过 5s", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @Schema(description = "商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "分类 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;


    @Schema(description = "创建人名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUserName;

    @Schema(description = "更新人名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUserName;

    @Schema(description = "触发条件设置 1.所有用户 2.新用户 3.老用户 4.回归用户", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer triggerCondition;

    @Schema(description = "弹出方式 1.默认弹出方式 2.自下向上弹出", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer popStyle;

    @Schema(description = "是否任何时间段(1是 2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAllTime;

    @Schema(description = "规则（1.周期内一次 2.周期内每天一次 3.每次进入）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ruler;

    @Schema(description = "类型（1.首页 2.点餐 3.订单及结算 4.其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer positionType;

    @Schema(description = "展示周期（1.长期展示 2.指定日期展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayPeriod;

    @Schema(description = "展示时段（1.全天展示 2.指定时间段展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer showTimeType;

    @Schema(description = "应用范围（1按门店  2按标签）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicationScope;

    @Schema(description = "旧删除", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isDelete;

    @Schema(description = "旧项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long projectOwnerShip;

    @Schema(description = "广告排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer advertisingSort;

    @Schema(description = "指定日期逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dayNumbers;


    @Schema(description = "指定周几逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String weekNumbers;





    @Schema(description = "指定时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String timeRange;



    @Schema(description = "人群字段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String crowds;


    @Schema(description = "系统判定是否启用(1 启用 2禁用)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isOpen;

    @TableField(exist = false)
    private List<Integer> dayNumberList;
    @TableField(exist = false)
    private List<Integer> weekNumberList;

    @Schema(description = "标签列表逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String tagNumbers;

    @TableField(exist = false)
    private List<Long> tagList;

    @Schema(description = "指定时间段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    private List<AdvertisingTimeDO> timeVOList;

    @Schema(description = "人群列表集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    private List<Long> crowdIds = new ArrayList<>();

    @Schema(description = "广告图片列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    private List<AdvertisingImageDO> imageDOList = new ArrayList<>();
    @TableField(exist = false)
    private List<Integer> floatingWindowDisplay = new ArrayList<>(); //浮窗显示页面 （0.全部  1.首页 2.订单结算页 3.点餐页 4.订单详情页 5.积分商城页 6.个人中心页）
    private Integer floatingIsAll; //浮窗是否全选 (1.是 2.否)
    @TableField(exist = false)
    private List<Long> storeIds = new ArrayList<>();; //门店 ids

    @Schema(description = "排列方式（1 横栏展示  2双栏展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer arrangeMethod;



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

    public void setCrowdIds(List<Long> crowdIds) {
        this.crowdIds = crowdIds;
        if(ObjectUtil.isNotEmpty(crowdIds)){
            this.crowds = crowdIds.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.crowds = null;
        }
    }

    public void setCrowds(String crowds) {
        this.crowds = crowds;
        if(ObjectUtil.isNotEmpty(crowds)){
            String[] array = crowds.split(",");
            this.crowdIds = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                this.crowdIds.add(Long.parseLong(array[i]));
            }
        }else {
            this.crowdIds = null;
        }
    }
}
