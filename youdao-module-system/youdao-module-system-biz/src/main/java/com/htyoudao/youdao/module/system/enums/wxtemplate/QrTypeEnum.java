package com.htyoudao.youdao.module.system.enums.wxtemplate;

// 二维码类型枚举
public enum QrTypeEnum {
    WECHAT(0, "微信二维码"),
    GROUP(1, "群二维码");

    private final Integer code;
    private final String desc;

    QrTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static QrTypeEnum fromCode(Integer code) {
        for (QrTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return WECHAT;
    }
}
