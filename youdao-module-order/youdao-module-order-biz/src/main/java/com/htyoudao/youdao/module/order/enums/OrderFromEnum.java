package com.htyoudao.youdao.module.order.enums;

import lombok.Getter;

@Getter
public enum OrderFromEnum {
    /**
     * 店内点餐机
     */
    POINT_SINGLE_MACHINE(0, "店内点餐机", "点餐机用户"),
    /**
     * 微信小程序
     */
    WECHAT_MINI_PROGRAM(1, "微信小程序", "微信用户"),
    /**
     * 支付宝小程序
     */
    ALIPAY_MINI_PROGRAM(2, "支付宝小程序", "支付宝用户"),
    /**
     * 抖音团购
     */
    TIKTOK(3, "抖音团购", "点餐机用户"),
    /**
     * 美团团购
     */
    MEITUAN(4, "美团团购", "点餐机用户");

    private final int code;

    private final String message;

    private final  String memberName;

    OrderFromEnum(int code, String message, String memberName) {
        this.code = code;
        this.message = message;
        this.memberName = memberName;
    }

    public static String getMessageByCode(int code) {
        OrderFromEnum[] values = OrderFromEnum.values();
        for (OrderFromEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }

    public static String getMemberNameByCode(int code) {
        OrderFromEnum[] values = OrderFromEnum.values();
        for (OrderFromEnum value : values) {
            if (code == value.getCode()) {
                return value.getMemberName();
            }
        }
        return null;
    }

    public static int getByCodeMessage(String message) {
        OrderFromEnum[] values = OrderFromEnum.values();
        for (OrderFromEnum value : values) {
            if (message.equals(value.getMessage())) {
                return value.getCode();
            }
        }
        return 0;
    }


}
