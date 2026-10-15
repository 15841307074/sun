package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 经营异常类型枚举
 *
 * <p>对应 {@code analysis_anomaly_user_report.anomaly_type} 字段。
 * 这个值同时决定了周期是按周还是按月，不需要再单独存一个 period_type。
 *
 * <ul>
 *   <li>1 = 周度异常（按周聚合）</li>
 *   <li>2 = 月度货品销量异常（按月聚合）</li>
 *   <li>3 = 月度货品 0 订单异常（按月聚合）</li>
 *   <li>10 及以后预留给后续扩展</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum AnomalyTypeEnum {

    WEEK_ANOMALY(1, "周度异常"),
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
     * 是否月度类型（2、3 是月度，1 是周度）
     */
    public boolean isMonthly() {
        return this == MONTH_PRODUCT_SALES || this == MONTH_ZERO_ORDER;
    }
}
