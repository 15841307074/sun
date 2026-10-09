package com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 副本管理 sku Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommoditySkuRespVO {

    /** SKU ID */
    @Schema(description = "SKU ID")
    private Long skuId;

    /** 关联商品 ID */
    @Schema(description = "商品ID")
    private Long commodityId;

    /** 规格名称 */
    @Schema(description = "规格名称")
    private String skusName;

    /** 规格值 */
    @Schema(description = "规格值")
    private String skusValue;

    /**
     * 平台价格
     */
    @Schema(description = "平台价格")
    private BigDecimal illustratePrices;

    /**
     * 规格配方列表
     */
    @Schema(description = "规格配方列表")
    private List<RecipeRespVO> recipeList = new ArrayList<>();

}
