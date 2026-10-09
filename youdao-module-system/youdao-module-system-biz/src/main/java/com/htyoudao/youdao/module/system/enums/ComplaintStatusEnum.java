package com.htyoudao.youdao.module.system.enums;

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
public enum ComplaintStatusEnum implements ArrayValuable<Integer> {

    NOTPROCESSED(0, "未处理"),
    PROCESSED(1, "已处理");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ComplaintStatusEnum::getStatus).toArray(Integer[]::new);

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
