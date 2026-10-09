package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderAmountRangeVO {

    @Schema(description = "优惠前总额")
    private Double orderAmount;

    @Schema(description = "打包费")
    private Double packingCharge;

    @Schema(description = "配送费")
    private Double minimumDeliveryFee;
}
