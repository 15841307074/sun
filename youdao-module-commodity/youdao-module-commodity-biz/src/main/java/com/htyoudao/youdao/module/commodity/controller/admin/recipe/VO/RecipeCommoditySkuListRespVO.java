package com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 配方管理 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommoditySkuListRespVO {

    @Schema(description = "商品ID")
    private Long commodityId;

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "商品所属分组名")
    private String categoryName;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "商品规格集合")
    private List<RecipeCommoditySkuRespVO> recipeCommoditySkuList;
}
