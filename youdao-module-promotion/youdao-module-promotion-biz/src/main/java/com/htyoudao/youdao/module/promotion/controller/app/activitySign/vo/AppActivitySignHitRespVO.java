package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "小程序 - 首页签到进度 Response VO")
@Data
public class AppActivitySignHitRespVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "是否展示首页签到进度模块；仅当前用户参与过且活动在活动期间内才展示")
    private Boolean showProgress;

    @Schema(description = "活动ID；showProgress=false时可为空")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动状态：0未开始 1进行中 2已结束 3已停用")
    private Integer activityStatus;

    @Schema(description = "活动封面图，取活动推广展示图")
    private String coverImage;

    @Schema(description = "首页进度模块按钮文案，按蓝湖返回：今日已签到/去签到/查看活动")
    private String buttonText;

    @Schema(description = "今日是否已签到")
    private Boolean todaySigned;

    @Schema(description = "下一个待获得奖励的当前进度天数；首页进度3/5中的3，没有下一个奖励时为空")
    private Integer rewardProgressDays;

    @Schema(description = "下一个待获得奖励要求的签到天数；首页进度3/5中的5，没有下一个奖励时为空")
    private Integer nextRewardTargetDays;

    @Schema(description = "距离下一个奖励还差几天；等于nextRewardTargetDays - rewardProgressDays，没有下一个奖励时为空")
    private Integer nextRewardNeedDays;

    @Schema(description = "是否已获得当前周期全部奖励")
    private Boolean rewardFinished;

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;
}
