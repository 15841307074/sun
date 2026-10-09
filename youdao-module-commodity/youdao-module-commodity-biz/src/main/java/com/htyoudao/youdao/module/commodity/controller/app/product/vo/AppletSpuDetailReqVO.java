package com.htyoudao.youdao.module.commodity.controller.app.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * @author dht
 */
@Schema(description = "app - 订单生成后加购商品 Request VO")
@Data
@ToString(callSuper = true)
public class AppletSpuDetailReqVO {

    @Schema(description = "门店id", example = "14445")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "分类id", example = "14445")
    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    @Schema(description = "商品id", example = "14445")
    @NotNull(message = "商品id不能为空")
    private Long spuId;
}
