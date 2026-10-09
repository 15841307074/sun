package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class ActivityDataAnalysisRespVO {


    @Schema(description = "付款用户数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer paymentUserQuantity;

    @Schema(description = "订单数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer OrderNumber;

    @Schema(description = "优惠总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal offerTotalAmount;

    @Schema(description = "支付总金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal paymentTotalAmount;


    @Schema(description = "客单价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal customerAmount;

}
