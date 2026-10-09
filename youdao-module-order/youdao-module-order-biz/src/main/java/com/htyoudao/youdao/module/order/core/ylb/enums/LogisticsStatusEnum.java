package com.htyoudao.youdao.module.order.core.ylb.enums;

import com.htyoudao.youdao.module.order.enums.OrderStateEnum;
import lombok.Getter;

@Getter
public enum LogisticsStatusEnum {
    /**
     * 骑手收餐入箱
     */
    RX(1, OrderStateEnum.DELIVERED.getCode(), "骑手收餐入箱"),

    /**
     * 骑手配送中
     */
    PS(2, OrderStateEnum.DELIVERED.getCode(), "骑手配送中"),

    /**
     * 骑手已送达
     */
    SD(3, OrderStateEnum.COMPLETED.getCode(), "骑手已送达"),

    /**
     * 商家出餐
     */
    CC(4, OrderStateEnum.MAKING.getCode(), "商家出餐"),

    /**
     * 骑手到店取餐
     */
    QC(5, OrderStateEnum.MAKING.getCode(), "骑手到店取餐");

    private int logisticsStatus;

    private int code;

    private String message;

    LogisticsStatusEnum(int logisticsStatus, int code, String message) {
        this.logisticsStatus = logisticsStatus;
        this.code = code;
        this.message = message;
    }

    public static String getMessageByCode(int code) {
        LogisticsStatusEnum[] values = LogisticsStatusEnum.values();
        for (LogisticsStatusEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }

    public static int getCodeByLogisticsStatus(int logisticsStatus) {
        LogisticsStatusEnum[] values = LogisticsStatusEnum.values();
        for (LogisticsStatusEnum value : values) {
            if (logisticsStatus == value.getLogisticsStatus()) {
                return value.getCode();
            }
        }
        return 0;
    }
}
