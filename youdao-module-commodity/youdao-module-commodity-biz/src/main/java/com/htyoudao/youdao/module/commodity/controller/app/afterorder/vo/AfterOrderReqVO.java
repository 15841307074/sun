package com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author dht
 */
@Schema(description = "app - 订单生成后加购商品 Request VO")
@Data
@ToString(callSuper = true)
public class AfterOrderReqVO {

    @Schema(description = "下单的门店id", example = "14445")
    @NotNull(message = "下单的门店id不能为空")
    private Long storeId;

    @Schema(description = "下单的商品ids", example = "14445")
    @NotNull(message = "下单的商品ids不能为空")
    private List<Long> commodityIds;
}
