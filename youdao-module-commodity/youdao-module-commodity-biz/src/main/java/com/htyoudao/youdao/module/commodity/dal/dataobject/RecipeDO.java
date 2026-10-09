package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.commodity.dal.dto.TimeBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
@TableName("commodity_sku_recipe")
public class RecipeDO extends BusinessBaseDO {

    @Schema(description = "商品规格配方ID")
    @TableId
    private Long recipeId;

    @Schema(description = "商品ID")
    private Long commodityId;

    @Schema(description = "商品规格ID")
    private Long skuId;

    @Schema(description = "原料类型名称")
    private String materialsTypeName;

    @Schema(description = "原料ID")
    private Long materialsId;

    @Schema(description = "原料编号")
    private String materialsCode;

    @Schema(description = "原料名称")
    private String materialsName;

    @Schema(description = "销量")
    private String consumption;

    @Schema(description = "使用单位")
    private String usedUnit;

    @Schema(description = "单位列表")
    private String unit;

    @Schema(description = "原料规格")
    private String specifications;

    @Schema(description = "是否允许替换 1 是 0否")
    private Long isAlternative;

    @Schema(description = "是否被关联 1 是")
    private Long relatedRecipeId;

}
