package com.htyoudao.youdao.module.commodity.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 加购商品状态枚举
 *
 * @author dht
 */
@Getter
@AllArgsConstructor
public enum AfterOrderStatusEnum implements ArrayValuable<Integer> {

    ONSHELF(0, "上架"),
    UNSHELF(1, "下架");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AfterOrderStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
