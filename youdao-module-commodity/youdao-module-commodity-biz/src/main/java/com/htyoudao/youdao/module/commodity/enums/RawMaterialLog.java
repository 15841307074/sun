package com.htyoudao.youdao.module.commodity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 原材料日志记录类型
 */
@Getter
@AllArgsConstructor
public enum RawMaterialLog {

    RECIPE_NOT_FOUND(1, "配方找不到"),
    STORE_MATERIAL_NOT_FOUND(2, "门店下原材料找不到"),
    CATEGORY_NO_MATERIAL(3, "门店分类下无对应原材料"),
    UNIT_MISMATCH(4, "单位不匹配"),
    WAREHOUSE_NOT_MATERIAL(5, "门店仓库下无此原料");

    private final Integer type;
    private final String description;
}
