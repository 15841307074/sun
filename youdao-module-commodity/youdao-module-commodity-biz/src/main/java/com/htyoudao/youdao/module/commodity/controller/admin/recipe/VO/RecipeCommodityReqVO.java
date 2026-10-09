package com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.commodity.enums.RecipeCommodityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommodityReqVO extends PageParam {
    @Schema(description = "配方设置标识")
    private Integer skuFlag;

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "所属组织")
    private Long orgId;

    @Schema(description = "是否是门店 0 否 1 是")
    private Integer isStore;

    @Schema(description = "连锁商品隐藏属性 0--显示 1--隐藏 2--全部")
    @InEnum(RecipeCommodityEnum.class)
    private Integer isHidden;

}
