package com.htyoudao.youdao.framework.common.enums;

import cn.hutool.core.util.ArrayUtil;
import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum WxAddressTypeEnum implements ArrayValuable<Integer> {
    DEFAULT(1, "默认"), // 面向 c 端，普通用户
    NORMAL(0, "非默认"); // 面向 b 端，管理后台;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WxAddressTypeEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;

    public static WxAddressTypeEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(addressType -> addressType.getValue().equals(value), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
