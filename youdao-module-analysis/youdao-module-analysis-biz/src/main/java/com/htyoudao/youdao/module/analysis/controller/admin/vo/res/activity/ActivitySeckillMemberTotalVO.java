package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActivitySeckillMemberTotalVO {
    @Schema(description = "参与人数")
    private Long memberCount;
    @Schema(description = "订单数")
    private Long orderNumber;
    @Schema(description = "支付金额")
    private Double totalAmount;
}