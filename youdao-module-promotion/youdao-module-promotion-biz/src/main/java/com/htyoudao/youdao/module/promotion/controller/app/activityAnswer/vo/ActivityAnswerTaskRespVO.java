package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 有奖问答任务响应。
 */
@Data
public class ActivityAnswerTaskRespVO {

    @Schema(description = "任务类型 free/sign/order/share/browse")
    private String taskType;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "任务状态 0未达上限可继续完成 1已完成(兼容旧值) 2已达上限")
    private Integer taskStatus;

    @Schema(description = "完成任务可获得答题次数")
    private Integer rewardChance;

    @Schema(description = "已完成次数")
    private Integer finishCount;

    @Schema(description = "最大完成次数")
    private Integer limitCount;

    @Schema(description = "下单金额门槛，不限制或非下单任务返回0")
    private BigDecimal paymentThreshold;
}
