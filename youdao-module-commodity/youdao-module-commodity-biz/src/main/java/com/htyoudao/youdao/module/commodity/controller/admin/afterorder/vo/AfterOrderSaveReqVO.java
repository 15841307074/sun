package com.htyoudao.youdao.module.commodity.controller.admin.afterorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 订单生成后加购商品新增/修改 Request VO")
@Data
public class AfterOrderSaveReqVO {

    @Schema(description = "订单生成后加购商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15719")
    private Long afterId;

    @Schema(description = "商品ID", example = "5573")
    private Long commodityId;

    @Schema(description = "加购价格", example = "14445")
    @NotNull(message = "加购价格不能为空")
    @DecimalMin(value = "0.000001", message = "加购价格必须大于0")
    @Digits(integer = 3, fraction = 2, message = "加购价格整数部分不超过3位，小数部分不超过2位")
    private BigDecimal afterPrice;
}