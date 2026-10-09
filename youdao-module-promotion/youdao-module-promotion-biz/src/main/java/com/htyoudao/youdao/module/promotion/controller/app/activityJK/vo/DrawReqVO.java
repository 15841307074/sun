package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽卡请求VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "抽卡请求")
public class DrawReqVO {

    @Schema(description = "活动ID", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long memberId;


    @Schema(description = "门店ID")
    private Long storeId;
}
