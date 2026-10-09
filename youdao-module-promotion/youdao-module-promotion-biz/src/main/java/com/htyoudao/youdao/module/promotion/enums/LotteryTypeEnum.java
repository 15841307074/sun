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
public enum LotteryTypeEnum implements ArrayValuable<Integer> {
    COUPON(1, "优惠卷"),
    INTEGRAL(2, "积分"),
    REAL(3, "实物"),
    NOPRIZES(4, "无奖品");
    public static final Integer[] ARRAYS = Arrays.stream(values()).map(LotteryTypeEnum::getStatus).toArray(Integer[]::new);

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
