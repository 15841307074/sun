package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
@TableName("tri_commodity_product_formulation")
public class TriFormulationDO extends BusinessBaseDO {

    @Schema(description = "三方商品ID")
    @TableId
    private Long id;

    @Schema(description = "三方渠道编号")
    private String triType;

    @Schema(description = "三方商品名称")
    private String triCommodityName;

    @Schema(description = "是否设置配方")
    private Integer recipeState;

    @Schema(description = "是否已经设置配方")
    private Integer skuFlag;

}
