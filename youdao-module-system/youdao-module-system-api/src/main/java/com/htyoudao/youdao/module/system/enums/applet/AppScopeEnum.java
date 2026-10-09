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
public enum AppScopeEnum implements ArrayValuable<Integer> {

    STORE(1, "门店范围"), // https://bpmn.io/toolkit/bpmn-js/
    TAG(2, "标签范围"); // 参考钉钉、飞书工作流的设计器

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AppScopeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
