package com.htyoudao.youdao.module.system.enums.wxtemplate;

// 发布状态枚举
public enum ReleaseStatus {
    DRAFT(0, "未发布"),
    PUBLISHED(1, "已发布");

    private final Integer code;
    private final String desc;

    ReleaseStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}