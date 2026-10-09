package com.htyoudao.youdao.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author 0090
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    T_00("00", "系统用户"),
    T_01("01", "新增客户"),
    T_02("02", "供应链员工"),
    T_03("03", "供应链客户员工");

    /**
     * 状态值
     */
    private final String status;
    /**
     * 状态名
     */
    private final String name;
}
