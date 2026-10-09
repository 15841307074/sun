package com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeMaterialUnSelectedReqVO {


    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "规格ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    @Schema(description = "配方ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recipeId;

    @Schema(description = "原料类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsTypeName;

    @Schema(description = "原料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsName;

    @Schema(description = "原料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long materialsId;

    @Schema(description = "原料替换主品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long replaceMaterialsId;

    @Schema(description = "原料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsCode;



}
