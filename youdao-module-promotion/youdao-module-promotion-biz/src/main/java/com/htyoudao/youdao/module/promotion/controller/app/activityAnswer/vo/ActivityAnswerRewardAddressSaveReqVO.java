package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 有奖问答实物奖励地址保存请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRewardAddressSaveReqVO extends ActivityAnswerBaseReqVO {

    @Schema(description = "奖励记录ID")
    @NotNull(message = "奖励记录ID不能为空")
    private Long rewardLogId;

    @Schema(description = "收货人")
    @NotBlank(message = "收货人不能为空")
    private String receiveUser;

    @Schema(description = "收货手机号")
    @NotBlank(message = "收货手机号不能为空")
    private String receiveMobile;

    @Schema(description = "收货地址")
    @NotBlank(message = "收货地址不能为空")
    private String receiveAddress;
}
