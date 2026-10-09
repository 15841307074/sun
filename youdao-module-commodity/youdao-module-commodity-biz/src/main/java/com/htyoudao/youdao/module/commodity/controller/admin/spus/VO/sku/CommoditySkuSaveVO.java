package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Schema(description = "管理后台 - 规格新增 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySkuSaveVO {

    /** 规格名称 */
    @Schema(description = "规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skusName;

    /** 规格值 */
    @Schema(description = "规格值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skusValue;

    private Long skuId;

    /**
     *  门店状态
     *  1上架 0下架
     */
    @Schema(description = "状态1上架 0下架", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态1上架 0下架 不能为空")
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
    @NotNull(message = "平台价格不能为空")
    private BigDecimal illustratePrices;
    /**
     * 划线价格
     */
    @Schema(description = "划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "划线价格不能为空")
    private BigDecimal strikeThroughPrice;
}
