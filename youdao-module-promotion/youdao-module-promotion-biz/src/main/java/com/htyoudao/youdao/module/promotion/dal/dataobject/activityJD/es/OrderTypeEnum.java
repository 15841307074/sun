package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es;

import lombok.Getter;

public enum OrderTypeEnum {

    /**
     * 堂食
     */
    CANTEEN_FOOD(0, "堂食"),

    /**
     * 打包
     */
    PACK(1, "打包"),

    /**
     * 外卖
     */
    TAKEAWAY(2, "外卖");

    @Getter
    private int code;

    @Getter
    private String message;

    OrderTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(int code) {
        OrderTypeEnum[] values = OrderTypeEnum.values();
        for (OrderTypeEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
