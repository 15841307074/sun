package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "抽签任务")
public class TaskVO {

    @Schema(description = "任务类型 1签到 2下单 3分享助力 4免费集签 5浏览首页")
    private Integer taskType;

    @Schema(description = "下单金额门槛")
    private BigDecimal amount;

    @Schema(description = "完成上限")
    private Integer taskLimit;

    @Schema(description = "完成数量")
    private Integer finishCount;
}
