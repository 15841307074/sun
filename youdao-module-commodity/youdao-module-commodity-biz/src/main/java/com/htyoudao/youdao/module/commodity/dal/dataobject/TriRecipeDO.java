package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
@TableName("tri_commodity_recipe")
public class TriRecipeDO extends BusinessBaseDO {

    @Schema(description = "商品规格配方ID")
    @TableId
    private Long recipeId;

    @Schema(description = "主表ID")
    private Long triId;

    @Schema(description = "三方渠道编号")
    private String triType;

    @Schema(description = "三方商品名称")
    private String triCommodityName;

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

    @Schema(description = "关联三方商品配方ID")
    private Long relatedRecipeId;
}
