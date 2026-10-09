package com.htyoudao.youdao.module.commodity.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 损耗类型
 */
@Getter
@AllArgsConstructor
public enum RecipeCommodityEnum implements ArrayValuable<Integer> {

    IS_SHOW(0, "显示"),

    IS_HIDDEN(1, "隐藏"),

    IS_ALL(2, "全部"),
;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(RecipeCommodityEnum::getValue).toArray(Integer[]::new);

    private final Integer value;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
