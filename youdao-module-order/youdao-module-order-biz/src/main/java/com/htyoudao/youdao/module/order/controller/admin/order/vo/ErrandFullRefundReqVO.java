package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "PC代取订单整单退款请求 VO")
public class ErrandFullRefundReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202606041000000001")
    @NotBlank(message = "订单号不能为空")
    private String orderSn;
}
