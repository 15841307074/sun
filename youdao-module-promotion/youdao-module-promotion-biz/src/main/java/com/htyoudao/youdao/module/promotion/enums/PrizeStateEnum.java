package com.htyoudao.youdao.module.promotion.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 通用状态枚举
 *
 * @author 0090
 */
@Getter
@AllArgsConstructor
public enum PrizeStateEnum implements ArrayValuable<Integer> {
    Issue(0, "已发放"),
    NOTFILLED(1, "商品类型  1未填写收货地址"),
    TOBESHIPPED(2, "已填写地址 待发货"),
    SHIPPED(3, "已发货"),
    RETURNPRIZE(9, "退回");
    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PrizeStateEnum::getStatus).toArray(Integer[]::new);

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
