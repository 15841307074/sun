package com.htyoudao.youdao.module.system.enums.wxtemplate;

// 模版状态枚举
public enum StateEnum {
    DISABLED(0, "默认模版"),
    ENABLED(1, "普通模版");

    private final Integer code;
    private final String desc;

    StateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}
