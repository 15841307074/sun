package com.htyoudao.youdao.module.commodity.controller.admin.category.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分组排序时使用
 */
@Data
public class CategorySortReqVo {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;


    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;
}
