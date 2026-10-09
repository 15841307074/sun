package com.htyoudao.youdao.module.order.enums;

import lombok.Getter;

/**
 * 拼单状态枚举 状态 0正常 1锁定 2取消 3完结
 *
 * @author youdao
 */
@Getter
public enum SplicingOrderStateEnum {

    /**
     * 正常
     */
    OK(0, "正常"),

    /**
     * 锁定
     */
    LOCK(1, "锁定"),

    /**
     * 取消
     */
    CANCELED(2, "取消"),

    /**
     * 完结
     */
    DONE(3, "完结");


    SplicingOrderStateEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;

    private final String message;

    public static String getMessageByCode(int code) {
        SplicingOrderStateEnum[] values = SplicingOrderStateEnum.values();
        for (SplicingOrderStateEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
