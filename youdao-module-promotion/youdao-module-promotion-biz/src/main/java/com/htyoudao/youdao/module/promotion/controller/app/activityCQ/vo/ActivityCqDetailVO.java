package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "抽签活动详情")
public class ActivityCqDetailVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;

    @Schema(description = "活动详情图")
    private String activityDetailsImage;

    @Schema(description = "按钮背景色")
    private String buttonBackgroundColor;

    @Schema(description = "活动开始时间")
    private Date activityStartTime;

    @Schema(description = "活动结束时间")
    private Date activityEndTime;

    @Schema(description = "活动规则")
    private String activityRule;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "签到开关")
    private Integer dailyAttendance;

    @Schema(description = "免费集签开关")
    private Integer freeEvent;

    @Schema(description = "免费抽签次数")
    private Integer freeCount;

    @Schema(description = "下单开关")
    private Integer placeOrderStatus;

    @Schema(description = "分享开关")
    private Integer shareEvent;

    @Schema(description = "浏览首页集签开关")
    private Integer browseHomeEvent;

    @Schema(description = "浏览首页每人每天上限获得次数")
    private Integer browseHomeCount;

    @Schema(description = "活动状态 0未开始 1已开始 2已开奖")
    private Integer activityStatus;

    @Schema(description = "支付门槛金额")
    private BigDecimal paymentThreshold;

    @Schema(description = "下单得码次数")
    private Integer placeOrderCodeNumber;

    @Schema(description = "分享得码次数")
    private Integer shareCount;

    @Schema(description = "结果公布时间")
    private LocalDateTime resultPublishTime;

    @Schema(description = "开奖状态 0未开奖 1开奖中 2已开奖")
    private Integer drawStatus;

    @Schema(description = "是否已参与")
    private Boolean joined;

    @Schema(description = "奖品列表")
    private List<PrizeVO> prizeList;
}
