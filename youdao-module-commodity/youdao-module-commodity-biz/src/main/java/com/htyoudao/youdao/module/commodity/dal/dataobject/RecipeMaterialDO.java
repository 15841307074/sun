package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
@TableName("commodity_recipe_material")
public class RecipeMaterialDO extends BusinessBaseDO {

    @Schema(description = "配方原料ID")
    @TableId
    private Long recipeMaterialId;

    @Schema(description = "配方ID")
    private Long recipeId;

    @Schema(description = "商品编号")
    private Long commodityId;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "原材料ID")
    private Long materialsId;

    @Schema(description = "原料编号")
    private String materialsCode;

    @Schema(description = "替换主品ID")
    private Long replaceMaterialsId;

    @Schema(description = "原料类型名称")
    private String materialsTypeName;

    @Schema(description = "原料名称")
    private String materialsName;
}
