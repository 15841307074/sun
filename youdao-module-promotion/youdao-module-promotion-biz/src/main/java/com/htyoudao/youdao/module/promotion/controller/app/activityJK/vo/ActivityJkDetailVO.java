package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 活动详情响应VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "活动详情响应")
public class ActivityJkDetailVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;

    @Schema(description = "我的奖励背景图")
    private String myBackgroundImage;

    @Schema(description = "抽卡按钮背景图")
    private String cardButtonBackgroundImage;

    @Schema(description = "任务按钮背景图")
    private String taskButtonBackgroundImage;

    @Schema(description = "活动详情长图")
    private String activityDetailsImage;

    @Schema(description = "按钮背景色")
    private String buttonBackgroundColor;

    @Schema(description = "活动开始时间")
    private Date activityStartTime;

    @Schema(description = "活动结束时间")
    private Date activityEndTime;

    @Schema(description = "活动规则")
    private String activityRule;

    @Schema(description = "每日签到开关 0关闭 1开启")
    private Integer dailyAttendance;

    @Schema(description = "下单送卡开关 0关闭 1开启")
    private Integer placeOrderStatus;

    @Schema(description = "分享活动开关 0关闭 1开启")
    private Integer shareEvent;

    @Schema(description = "浏览首页开关 0关闭 1开启")
    private Integer browseType;

    @Schema(description = "浏览首页送卡次数")
    private Integer browseCount;

    @Schema(description = "免费集卡开关 0关闭 1开启")
    private Integer freeStatus;

    @Schema(description = "免费集卡次数")
    private Integer freeCount;

    @Schema(description = "分享类型 1不允许 2允许好友 3允许复制链接")
    private Integer shareType;

    @Schema(description = "公共展示开关 0开启 1关闭")
    private Integer publicButton;


    @Schema(description = "活动状态 0未开始 1进行中 2已结束")
    private Integer activityStatus;

    @Schema(description = "支付门槛 0代表不限制")
    private BigDecimal paymentThreshold;

    @Schema(description = "下单送卡次数")
    private Integer placeOrderCardNumber;

    @Schema(description = "分享送卡次数")
    private Integer shareCount;

    @Schema(description = "卡片列表")
    private List<ActivityJkCardDO> activityCardVOList;

}
