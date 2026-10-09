package com.htyoudao.youdao.module.member.util.enums;

import lombok.Getter;

public enum SendStatusEnum {
    UNSHIPPED(1,"未发货"),
    SHIPPED(2,"已发货"),
    RECEIVED(3,"已收货")
    ;


    @Getter
    private int code;

    @Getter
    private String msg;

    SendStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(int code){
        SendStatusEnum[] values = SendStatusEnum.values();
        for (SendStatusEnum value : values) {
            if (value.getCode()==code){
                return value.getMsg();
            }
        }
        return null;
    }
}
