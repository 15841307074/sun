package com.htyoudao.youdao.module.system.enums.wxtemplate;

// 应用范围枚举
public enum ApplicationScope {
    ALL_STORES(1, "全部门店"),
    BY_TAG(2, "按标签");

    private final Integer code;
    private final String desc;

    ApplicationScope(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}
