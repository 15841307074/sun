package com.htyoudao.youdao.module.order.controller.app.pay.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrderPayReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7777287611060667438L;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202505010118191900423188")
    @NotBlank(message = "订单号不能为空")
    private String orderSn;

    @Schema(description = "二次支付标识 0否 1是", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer isRepeatPay;

    @Schema(description = "B扫C用户码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1234678")
    private String dynamicId;
}
