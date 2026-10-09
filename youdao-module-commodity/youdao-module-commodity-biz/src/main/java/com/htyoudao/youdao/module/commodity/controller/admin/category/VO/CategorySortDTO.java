package com.htyoudao.youdao.module.commodity.controller.admin.category.VO;

import lombok.Data;

/**
 * 分组排序时使用
 */
@Data
public class CategorySortDTO {
    private Long categoryId;
    private Integer sort;
}
