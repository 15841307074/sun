package com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeReqVO {

    @Schema(description = "配方ID")
    private Long recipeId;

    @Schema(description = "商品ID")
    @NotNull(message = "连锁商品编号不能为空")
    private Long commodityId;

    @Schema(description = "商品规格ID")
    @NotNull(message = "商品规格ID不能为空")
    private Long skuId;

    @Schema(description = "原料类型名称")
    @NotNull(message = "原料类型名称不能为空")
    private String materialsTypeName;

    @Schema(description = "原料ID")
    @NotNull(message = "原料ID不能为空")
    private Long materialsId;

    @Schema(description = "原料编号")
    @NotNull(message = "原料编号不能为空")
    private String materialsCode;

    @Schema(description = "原料名称")
    @NotNull(message = "原料名称不能为空")
    private String materialsName;

    @Schema(description = "消耗量")
    @NotNull(message = "消耗量不能为空")
    private String consumption;

    @Schema(description = "使用单位")
    @NotNull(message = "使用单位不能为空")
    private String usedUnit;

    @Schema(description = "单位列表")
    @NotNull(message = "单位列表不能为空")
    private String unit;

    @Schema(description = "原料规格")
    @NotNull(message = "原料规格不能为空")
    private String specifications;

    @Schema(description = "是否允许替换 1 是 0否")
    @NotNull(message = "是否允许替换不能为空")
    private Long isAlternative;

    @Schema(description = "是否被关联 1 是")
    private Long relatedRecipeId;
}
