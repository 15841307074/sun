package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityVoteUpdateExpressReqVO {

    @Schema(description = "奖励记录id")
    @NotNull(message = "记录id不能为空")
    private Long id;

    @Schema(description = "快递公司")
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @Schema(description = "快递单号")
    @NotBlank(message = "快递单号不能为空")
    private String trackingNumber;
}
