package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "抽签请求")
public class DrawReqVO {

    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "用户ID，后端从 token 获取", hidden = true)
    private Long memberId;

    @Schema(description = "门店ID")
    private Long storeId;
}
