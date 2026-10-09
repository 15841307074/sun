package com.htyoudao.youdao.module.promotion.dal.dataobject.advertisingImage;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 广告图片
 */
@TableName("advertising_image")
@Data
public class AdvertisingImageDO extends BusinessBaseDO {


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adUrl;

    @Schema(description = "图片是否跳转 1是 2否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isImageJump;

    @Schema(description = "图片跳转位置 (图片跳转位置 (0.无跳转 1.优惠卷 2.优惠卷包 3.小程序页面 4.内部链接 5.外部链接 6.周周惠优惠卷 7.营销活动 8.社群领劵 9.H5链接 10.商品详情))", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer jumpLocation;

    @Schema(description = "链接地址（如果内部链接 内部链接地址 如果是外部链接 外部链接地址 其它没有）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jumpUrl;

    @Schema(description = "优惠卷", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponId;

    @Schema(description = "优惠卷包", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponBagId;


    @Schema(description = "优惠卷名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;


    @Schema(description = "活动id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityId;

    @Schema(description = "活动类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityType;

    @Schema(description = "抽奖活动类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lotteryType;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponBagName;

    @Schema(description = "小程序页面", requiredMode = Schema.RequiredMode.REQUIRED)
    private String programPage;

    @Schema(description = "appId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long advertisingId;


    @Schema(description = "图片状态(1静态 2动态)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer imageStatus;

    @Schema(description = "动态图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dynamicImage;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


    @Schema(description = "媒体类型：1-图片，2-视频", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer mediaType;

    @TableField(exist = false)
    @Schema(description = "触发条件设置 1.所有用户弹出 2。新用户弹出 3.老用户弹出 4.回归用户弹出", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer triggerCondition;//触发条件设置 1.所有用户 2.新用户 3.老用户 4.回归用户

    @TableField(exist = false)
    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String activityName;

    @TableField(exist = false)
    @Schema(description = "规则（1.周期内一次 2.周期内每天一次 3.每次进入）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ruler;

    @TableField(exist = false)
    @Schema(description = "浮窗显示页面 （0.全部  1.首页 2.订单结算页 3.点餐页 4.订单详情页 5.积分商城页 6.个人中心页）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> floatingWindowDisplay = new ArrayList<>();

    @TableField(exist = false)
    @Schema(description = "弹出方式(1.默认弹出方式 2.自下向上弹出)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer popStyle;

    @TableField(exist = false)
    @Schema(description = "广告详细位置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition; //广告详细位置 （1.首页弹屏 2.首页轮播图 3.首页底部 banner 4.浮窗 5.点餐页弹屏 6.点餐页轮播图 7.点餐页商品分类 banner 8.结算页广告 9订单详情页广告，10订单列表页banner。11开屏）

    @TableField(exist = false)
    @Schema(description = "排列方式（1 横栏展示  2双栏展示）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer arrangeMethod;



}
