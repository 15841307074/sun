package com.htyoudao.youdao.module.commodity.dal.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SkuDto implements Serializable {

    @Schema(description = "门店SKU_ID")
    private Long skuId;

    @Schema(description = "原始 SKU_ID")
    private Long commoditySkuId;

    @Schema(description = "sku名称")
    private String skuName;

    @Schema(description = "门店下规格的值")
    private String skuValue;

    @Schema(description = "门店下当前规格的售卖价格")
    private BigDecimal skuPrice;

    @Schema(description = "门店下当前规格的状态")
    private Integer commodityStoreSkuStatus;

    @Schema(description = "门店下当前规格的划线价格")
    private BigDecimal commodityStoreSkuStrikePrice;
}
