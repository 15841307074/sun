package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 分类 VO")
public class CategoryDto extends TimeBaseDTO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "类型 是否下单必选分组 1 是 0否")
    private Integer type;

    @Schema(description = "顺序")
    private Integer sort;

    @Schema(description = "门店商品分类状态")
    private Integer commodityStoreCategoryStatus;

}
