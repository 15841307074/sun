package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Schema(description = "管理后台 - sku Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySkuRespVO {

    /** SKU ID */
    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    /** 关联商品 ID */
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;



    /** 规格名称 */
    @Schema(description = "规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skusName;

    /** 规格值 */
    @Schema(description = "规格值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skusValue;



    /**
     *  门店状态
     *  1上架 0下架
     */
    @Schema(description = "门店状态  1上架 0下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private int storeStatus;
    /**
     *  微信状态 暂时弃用，字段保留
     *
     */
    @Schema(description = "微信状态 暂时弃用，字段保留", requiredMode = Schema.RequiredMode.REQUIRED)
    private int wxStatus;

    /**
     * 平台价格
     */
    @Schema(description = "平台价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal illustratePrices;
    /**
     * 划线价格
     */
    @Schema(description = "划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal strikeThroughPrice;





}
