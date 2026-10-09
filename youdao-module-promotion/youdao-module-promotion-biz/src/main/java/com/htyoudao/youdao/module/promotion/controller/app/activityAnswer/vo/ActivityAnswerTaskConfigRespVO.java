package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答任务配置响应。
 */
@Data
public class ActivityAnswerTaskConfigRespVO {

    @Schema(description = "免费获得开关 0关闭 1开启")
    private Integer freeStatus;

    @Schema(description = "免费获得次数")
    private Integer freeCount;

    @Schema(description = "签到开关 0关闭 1开启")
    private Integer signStatus;

    @Schema(description = "签到获得次数")
    private Integer signCount;

    @Schema(description = "下单开关 0关闭 1开启")
    private Integer orderStatus;

    @Schema(description = "下单获得次数")
    private Integer placeOrderAnswerNumber;

    @Schema(description = "分享开关 0关闭 1开启")
    private Integer shareEvent;

    @Schema(description = "分享获得次数")
    private Integer shareCount;

    @Schema(description = "浏览首页开关 0关闭 1开启")
    private Integer browseType;

    @Schema(description = "浏览首页获得次数")
    private Integer browseCount;
}
