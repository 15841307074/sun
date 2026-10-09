package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppVoteReqVO {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "门店id")
    private Long storeId;

    @Schema(description = "会员id（后端填充）")
    private Long memberId;
}
