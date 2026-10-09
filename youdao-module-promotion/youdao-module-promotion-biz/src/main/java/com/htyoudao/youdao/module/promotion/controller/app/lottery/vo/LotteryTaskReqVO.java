package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "抽奖任务请求")
public class LotteryTaskReqVO {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "抽奖活动ID")
    private Long lotteryId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "当前用户ID")
    private Long memberId;
}