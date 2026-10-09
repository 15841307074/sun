package com.htyoudao.youdao.module.system.enums.wxtemplate;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 通用状态枚举
 *
 * @author 0090
 */
@Getter
@AllArgsConstructor
public enum StoreOpenStatusEnum implements ArrayValuable<Integer> {

    OPEN(0, "营业"),
    SHUTDOWN(1, "歇业");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(StoreOpenStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static String getMessageByCode(int status) {
        StoreOpenStatusEnum[] values = StoreOpenStatusEnum.values();
        for (StoreOpenStatusEnum value : values) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return null;
    }


}
