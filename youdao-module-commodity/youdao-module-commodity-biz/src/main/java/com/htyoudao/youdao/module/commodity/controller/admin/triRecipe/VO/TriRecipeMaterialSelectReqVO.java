package com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriRecipeMaterialSelectReqVO {

    @Schema(description = "三方商品配方ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recipeId;

    @Schema(description = "三方商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long triId;

    @Schema(description = "三方渠道类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triType;

    @Schema(description = "三方商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triCommodityName;

    @Schema(description = "原料类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsTypeName;

    @Schema(description = "原料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long materialsId;

    @Schema(description = "原料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsCode;

    @Schema(description = "原材料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialsName;

    @Schema(description = "规格单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Schema(description = "消耗量")
    private String consumption;

    @Schema(description = "使用单位")
    private String usedUnit;

    @Schema(description = "原料规格")
    private String specifications;

    @Schema(description = "是否允许替换 1 是 0否")
    private Long isAlternative;

}
