package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActivitySeckillOrderVO {



    @Schema(description = "订单数")
    private Long orderNumber;


    @Schema(description = "付款用户数")
    private Long customerCount;

    /**
     * 优惠总金额
     */
    @Schema(description = "优惠总金额")
    private Double offerAmount = 0.0;

    /**
     * 支付总金额
     */
    @Schema(description = "支付总金额")
    private Double payAmount = 0.0;

    /**
     * 客单价
     */
    @Schema(description = "客单价")
    private Double averagePayment;


}