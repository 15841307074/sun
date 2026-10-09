package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es;

import lombok.Getter;

/**
 * @author dht
 * 从order服务复制的
 */
@Getter
public enum OrderStateEnum {

    /**
     * 已取消
     */
    CANCELED(0, "已取消"),

    /**
     * 待付款
     */
    UNPAID(10, "待付款"),

    /**
     * 已取餐
     */
    TO_BE_PICKED_UP(30, "已取餐"),

    /**
     * 待配送
     */
    W_TO_BE_DELIVERED(40, "待配送"),

    /**
     * 配送中
     */
    DELIVERED(50, "配送中"),

    /**
     * 已完成
     */
    COMPLETED(60, "已完成"),

    /**
     * 已退款
     */
    PENDING_REFUND(70, "已退款"),

    /**
     * 制作中
     */
    MAKING(80, "制作中"),

    /**
     * 待取餐
     */
    W_TO_BE_PICKED_UP(200, "待取餐");


    OrderStateEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;

    private final String message;

    public static String getMessageByCode(int code) {
        OrderStateEnum[] values = OrderStateEnum.values();
        for (OrderStateEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
