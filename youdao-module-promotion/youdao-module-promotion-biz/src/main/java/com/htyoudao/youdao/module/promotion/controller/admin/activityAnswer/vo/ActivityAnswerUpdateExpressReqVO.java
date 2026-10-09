package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityAnswerUpdateExpressReqVO {

    @Schema(description = "发奖记录ID")
    @NotNull(message = "发奖记录ID不能为空")
    private Long id;

    @Schema(description = "快递公司")
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @Schema(description = "快递单号")
    @NotBlank(message = "快递单号不能为空")
    private String trackingNumber;
}
