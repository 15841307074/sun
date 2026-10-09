package com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO;

import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriFormulationRelatedRespVO extends PageParam {

    @Schema(description = "三方商品ID")
    private Long id;

    @Schema(description = "三方渠道编号")
    private String triType;

    @Schema(description = "三方商品名称")
    private String triCommodityName;

    @Schema(description = "是否设置配方")
    private Integer recipeState;

    @Schema(description = "是否已经设置配方")
    private Integer skuFlag;

    private List<TriRecipeRespVO> recipeList;

}
