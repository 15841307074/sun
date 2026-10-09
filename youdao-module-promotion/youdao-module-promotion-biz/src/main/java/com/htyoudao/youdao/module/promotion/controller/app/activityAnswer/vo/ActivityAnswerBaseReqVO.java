package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 有奖问答小程序基础请求。
 */
@Data
public class ActivityAnswerBaseReqVO {

    @Schema(description = "活动ID")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "门店ID")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "会员ID，后端以登录态覆盖")
    private Long memberId;
}
