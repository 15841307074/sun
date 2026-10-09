package com.htyoudao.youdao.module.commodity.controller.admin.template.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模板分类下商品排序用
 */
@Data
public class CommodityTemplateSortSpuReqVO {

    @NotNull(message = "模板商品id不能为空")
    @Schema(description = "模板分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;
}
