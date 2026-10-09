package com.htyoudao.youdao.module.order.controller.app.order.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "跑腿订单动作请求 VO，用于接单、已取货等只需要订单号的操作")
public class ErrandOrderActionReqVO {

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202606041000000001")
    @NotBlank(message = "订单号不能为空")
    private String orderSn;
}
