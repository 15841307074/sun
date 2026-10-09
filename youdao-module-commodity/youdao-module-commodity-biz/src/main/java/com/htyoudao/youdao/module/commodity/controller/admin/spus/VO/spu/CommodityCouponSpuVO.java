package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 优惠券 商品选择器
 */
@Data
public class CommodityCouponSpuVO  {

    /**
     * 分类 ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;
    /**
     * 商品 ID
     */
    @Schema(description = "商品ID")
    private Long commodityId;
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "分类名")
    private String categoryName;

    @Schema(description = "最低价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal lowPrice;

    @Schema(description = "最高价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal highPrice;

    @Schema(description = "最低划线价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal lowStrikePrice;

    @Schema(description = "最高划线价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal highStrikePrice;

    @Schema(description = "是否多规格",requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isMoreSku;

}
