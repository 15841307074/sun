package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "抽奖任务信息")
public class LotteryTaskVO {

    @Schema(description = "任务类型 3下单 4分享 5浏览首页")
    private Integer taskType;

    @Schema(description = "任务完成上限，-1 代表不限制")
    private Integer taskLimit;

    @Schema(description = "任务已完成数量")
    private Integer finishCount;

    @Schema(description = "当前可用抽奖次数")
    private Integer availableCount;

    @Schema(description = "下单金额门槛，不限制时返回0")
    private BigDecimal paymentThreshold;
} 
