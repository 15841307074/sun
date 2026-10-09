package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 */

@Data
public class StoreSingleSkuVO {

    /**
     * 门店下商品的SKUID
     */
    @Schema(description = "门店下商品的SKUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSkuId;

    /**
     * 门店下当前规格的售卖价格
     */
    @Schema(description = "门店下当前规格的售卖价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commodityStoreSkuPrice;

    /**
     * 门店下当前规格的划线价格
     */
    @Schema(description = "门店下当前规格的售卖价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commodityStoreSkuStrikePrice;

    @Schema(description = "门店下当前规格的连锁商品库的 skuId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;
}
