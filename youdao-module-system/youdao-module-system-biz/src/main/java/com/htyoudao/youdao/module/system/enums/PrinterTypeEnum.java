package com.htyoudao.youdao.module.system.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 打印机相关枚举类
 *
 * @author 0090
 */
@Getter
@AllArgsConstructor
public enum PrinterTypeEnum implements ArrayValuable<Integer> {

    STORE(1, "店铺的打印信息"),
    MEMBER(3, "会员的打印信息"),
    KITCHEN(2, "厨房的打印信息"),
    DELIVERY(4, "配送的打印信息");


    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PrinterTypeEnum::getStatus).toArray(Integer[]::new);

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
    public static PrinterTypeEnum fromStatus(int status) {
        return Arrays.stream(values())
                .filter(enumConstant -> enumConstant.getStatus() == status)
                .findFirst()
                .orElseThrow(() -> ServiceExceptionUtil.exception(ErrorCodeConstants.DICT_TYPE_NOT_EXISTS));
    }

}
