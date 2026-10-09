package com.htyoudao.youdao.module.system.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil;
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
public enum PrinterTomplateTypeEnum implements ArrayValuable<Integer> {

    STORE(1, "门店"),
    MEMBER(3, "顾客"),
    KITCHEN(2, "后厨"),
    DELIVERY(4, "配送");
    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PrinterTomplateTypeEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final int status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
    /**
     * 根据状态值获取枚举实例
     *
     * @param status 状态值
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果找不到对应的枚举实例
     */
    public static PrinterTomplateTypeEnum fromStatus(int status) {
        return Arrays.stream(values())
                .filter(enumConstant -> enumConstant.getStatus() == status)
                .findFirst()
                .orElseThrow(() -> ServiceExceptionUtil.exception(ErrorCodeConstants.DICT_TYPE_NOT_EXISTS));
    }

}
