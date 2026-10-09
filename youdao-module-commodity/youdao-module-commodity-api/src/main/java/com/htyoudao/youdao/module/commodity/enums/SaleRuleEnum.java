package com.htyoudao.youdao.module.commodity.enums;

import lombok.Getter;

/**
 * 销售规则枚举
 * 售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）
 *
 * @author youdao
 */
@Getter
public enum SaleRuleEnum {

    /**
     * 正常售卖
     */
    SALE_OK(0, "正常售卖"),

    /**
     * 仅套餐售卖
     */
    SETMEAL_SALE_OK(1, "仅套餐售卖"),

    /**
     * 仅小料售卖
     */
    CONDIMENT_SALE_OK(2, "仅小料售卖"),

    /**
     * 仅兑换售卖
     */
    EXCHANGE_SALE_OK(3, "仅兑换售卖");


    SaleRuleEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;

    private final String message;

    public static String getMessageByCode(int code) {
        SaleRuleEnum[] values = SaleRuleEnum.values();
        for (SaleRuleEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
