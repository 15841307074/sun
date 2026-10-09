package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 奖品兑换请求 VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "奖品兑换请求")
public class PrizeExchangeReqVO {

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    @Schema(description = "奖品ID", required = true)
    @NotNull(message = "奖品ID不能为空")
    private Long prizeId;

    @Schema(description = "storeId", required = true)
    @NotNull(message = "storeId不能为空")
    private Long storeId;
}