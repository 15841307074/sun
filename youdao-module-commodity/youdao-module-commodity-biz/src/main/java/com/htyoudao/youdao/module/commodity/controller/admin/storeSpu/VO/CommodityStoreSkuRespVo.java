package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 商品sku列表 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreSkuRespVo {


    @Schema(description = "门店下商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSkuId;


    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;


    @Schema(description = "门店下商品的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;


    @Schema(description = "门店下规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSkuName;


    @Schema(description = "门店下规格的值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSkuValue;


    @Schema(description = "门店下当前规格的售卖价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下当前规格的售卖价格")
    private BigDecimal commodityStoreSkuPrice;


    @Schema(description = "门店下当前规格的划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下当前规格的划线价格")
    private BigDecimal commodityStoreSkuStrikePrice;


    @Schema(description = "门店下当前规格的库存", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSkuStockQuantity;


    @Schema(description = "门店下当前规格的状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSkuStatus;



    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

}
