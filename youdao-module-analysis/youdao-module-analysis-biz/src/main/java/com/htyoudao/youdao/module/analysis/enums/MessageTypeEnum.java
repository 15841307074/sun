package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 平台消息类型枚举
 *
 * <p>对应 {@code analysis_platform_message.message_type} 字段。
 *
 * <p>这是一张通用消息表，经营异常提醒只是其中一种类型，
 * 将来加「售卖异常提醒」只需要往这里加一个值，不用改表结构。
 *
 * <p>值的含义和 {@link AnomalyTypeEnum} 一一对应：
 * 经营异常报告作为一个类型枚举写入消息表。
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    WEEK_ANOMALY(1, "周度经营异常"),
    MONTH_PRODUCT_SALES(2, "月度货品销量异常"),
    MONTH_ZERO_ORDER(3, "月度货品0订单异常");

    /**
     * 类型值，存入数据库的 TINYINT
     */
    private final Integer type;

    /**
     * 描述，仅用于日志 / 展示
     */
    private final String description;

    /**
     * 按 type 值反查枚举，找不到返回 null
     */
    public static MessageTypeEnum valueOfType(Integer type) {
        return Arrays.stream(values())
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
