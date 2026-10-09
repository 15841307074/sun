package com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分组排序时使用
 */
@Data
public class CategoryStoreSortVO {
    @NotNull
    @Schema(description = "门店下分类的唯一ID")
    private Long categoryId;

    @NotNull
    @Schema(description = "门店下分类顺序")
    private Integer sort;

    @NotNull
    @Schema(description = "门店ID")
    private Long storeId;
}
