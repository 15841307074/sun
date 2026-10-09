package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 万能卡兑换请求VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "万能卡兑换请求")
public class UniversalExchangeReqVO {

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @Schema(description = "目标卡片ID", required = true)
    @NotNull(message = "目标卡片ID不能为空")
    private Long targetCardId;
}
