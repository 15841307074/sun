package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "代取订单退款请求 VO")
public class ErrandRefundReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202606041000000001")
    @NotBlank(message = "订单号不能为空")
    private String orderSn;

    @Schema(description = "退款类型：FULL-整单退款 FOOD-餐费退款 REWARD-赏金退款",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "REWARD")
    @NotBlank(message = "退款类型不能为空")
    private String refundType;
}
