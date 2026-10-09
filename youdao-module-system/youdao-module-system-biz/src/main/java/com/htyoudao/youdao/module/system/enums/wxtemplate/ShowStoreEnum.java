package com.htyoudao.youdao.module.system.enums.wxtemplate;

// 展示门店枚举
public enum ShowStoreEnum {
    ALL(1, "全部门店"),
    PARTIAL(2, "部分门店");

    private final Integer code;
    private final String desc;

    ShowStoreEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}
