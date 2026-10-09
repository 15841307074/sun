package com.htyoudao.youdao.module.system.enums.applet;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 模型的类型的枚举
 *
 * @author 0090
 */
@Getter
@AllArgsConstructor
public enum StoreScopeEnum implements ArrayValuable<Integer> {

    ALL(1, "全部门店"), // https://bpmn.io/toolkit/bpmn-js/
    PART(2, "部分门店"); // 参考钉钉、飞书工作流的设计器

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(StoreScopeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
