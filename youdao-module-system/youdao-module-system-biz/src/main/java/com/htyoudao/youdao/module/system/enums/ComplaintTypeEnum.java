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
public enum ComplaintTypeEnum implements ArrayValuable<Integer> {

    OTHERCOMPLAINTS(0, "其他投诉"),
    PRODUCTSCOMPLAINTS(1, "产品投诉"),
    SERVECOMPLAINTS(2, "服务投诉"),
    HYGIENECOMPLAINTS(3, "卫生投诉");


    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ComplaintTypeEnum::getStatus).toArray(Integer[]::new);

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
