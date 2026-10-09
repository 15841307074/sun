package com.htyoudao.youdao.module.system.enums;

import cn.hutool.core.util.ObjUtil;
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
public enum StoreUserEnum implements ArrayValuable<Integer> {

    STOREMANAGER(1, "店长"),
    MANGER(2, "门店经理");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(StoreUserEnum::getStatus).toArray(Integer[]::new);

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
