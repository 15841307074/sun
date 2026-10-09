package com.htyoudao.youdao.module.commodity.enums;

import lombok.Getter;

/**
 * 损耗类型
 */
@Getter
public enum LossType {

    /**
     * 样余 - 样品剩余导致的损耗
     */
    SAMPLE_REMAINDER(1, "样余"),

    /**
     * 炸糊 - 食品炸制过度导致的损耗
     */
    OVER_FRIED(2, "炸糊"),

    /**
     * 餐品不达标 - 餐品质量不符合标准导致的损耗
     */
    SUBSTANDARD_MEAL(3, "餐品不达标"),

    /**
     * 丢餐 - 餐品丢失导致的损耗
     */
    LOST_MEAL(4, "丢餐"),

    /**
     * 员工餐 - 员工用餐产生的损耗
     */
    STAFF_MEAL(5, "员工餐"),

    /**
     * 留样 - 食品留样产生的损耗
     */
    SAMPLE_RETAINED(6, "留样"),

    /**
     * 试餐尝餐 - 试吃或品尝产生的损耗
     */
    TASTING(7, "试餐"),

    /**
     * 活动赠送 - 活动赠送产生的损耗
     */
    ACTIVITY_GIFT(8, "活动赠送"),

    /**
     * 外卖损耗 - 外卖过程中产生的损耗
     */
    DELIVERY_LOSS(9, "外卖损耗");

    private final Integer code;
    private final String description;



    LossType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     * @param code 类型代码
     * @return 对应的枚举值，如果找不到返回null
     */
    public static LossType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LossType type : LossType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取类型代码
     * @return 类型代码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取类型描述
     * @return 类型描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 检查是否为指定代码
     * @param code 要检查的代码
     * @return 如果匹配返回true，否则返回false
     */
    public boolean isCode(Integer code) {
        if (code == null) {
            return false;
        }
        return this.code.equals(code);
    }

    @Override
    public String toString() {
        return this.description;
    }
}
