package com.htyoudao.youdao.module.analysis.enums;

import java.util.List;

/**
 * 经营异常提醒定时任务类型枚举
 *
 * <p>对应 {@code analysis_anomaly_job_record.job_type} 字段。
 * 枚举值同时包含 XXL-Job handler 名称，一处维护。
 *
 * <p>依赖关系（箭头从前置任务指向后续任务）：
 * <pre>
 * 周快照(1) + 上游周异常(9) → 周聚合(3) → 周推送(6)
 * 月快照(2) + 上游月销量(10) → 月销量聚合(4) → 月销量推送(7)
 * 月快照(2) + 上游月零订单(11) → 月0订单聚合(5) → 月0订单推送(8)
 * </pre>
 */
public enum AnomalyJobTypeEnum {

    // ==================== 快照 ====================

    /** 周快照：每周一 06:00，保存人店关系 */
    SNAPSHOT_WEEK(1, "周快照", "analysisUserStoreSnapshotWeekJobHandler"),

    /** 月快照：每月 1 号 06:00，保存人店关系 */
    SNAPSHOT_MONTH(2, "月快照", "analysisUserStoreSnapshotMonthJobHandler"),

    // ==================== 上游数据任务 ====================
    // 常量需先声明供聚合引用；编号仍接续原有1~8，严禁用ordinal代替type。
    /** 周度投诉异常指标计算完成标记，无本模块前置依赖。 */
    SOURCE_WEEK_ANOMALY(9, "周度投诉异常指标计算", "analysisComplaintAnomalyWeekJobHandler"),
    /** 上游生成月度货品销量异常数据。 */
    SOURCE_MONTH_PRODUCT_SALES(10, "月度货品销量异常数据生成", "analysisSourceMonthProductSalesJobHandler"),
    /** 上游生成月度货品零订单异常数据。 */
    SOURCE_MONTH_ZERO_ORDER(11, "月度货品零订单异常数据生成", "analysisSourceMonthZeroOrderJobHandler"),

    // ==================== 报告聚合 ====================

    /** 周聚合：每周一 07:00，从周表取异常门店按人聚合 */
    REPORT_WEEK(3, "周聚合", "analysisWeekAnomalyJobHandler", SNAPSHOT_WEEK, SOURCE_WEEK_ANOMALY),

    /** 月销量聚合：每月 1 号 07:00，从销量异常月表取异常门店按人聚合 */
    REPORT_MONTH_SALES(4, "月销量聚合", "analysisMonthProductSalesJobHandler", SNAPSHOT_MONTH, SOURCE_MONTH_PRODUCT_SALES),

    /** 月0订单聚合：每月 1 号 07:00，从0订单异常月表取异常门店按人聚合 */
    REPORT_MONTH_ZERO(5, "月0订单聚合", "analysisMonthZeroOrderJobHandler", SNAPSHOT_MONTH, SOURCE_MONTH_ZERO_ORDER),

    // ==================== 推送 ====================

    /** 周推送：每周一 09:00，落消息+调云函数 */
    PUSH_WEEK(6, "周推送", "analysisWeekAnomalyPushJobHandler", REPORT_WEEK),

    /** 月销量推送：每月 1 号 09:00，落消息+调云函数 */
    PUSH_MONTH_SALES(7, "月销量推送", "analysisMonthProductSalesPushJobHandler", REPORT_MONTH_SALES),

    /** 月0订单推送：每月 1 号 09:00，落消息+调云函数 */
    PUSH_MONTH_ZERO(8, "月0订单推送", "analysisMonthZeroOrderPushJobHandler", REPORT_MONTH_ZERO);

    /**
     * 类型值，存入数据库的 TINYINT
     */
    private final Integer type;

    /**
     * 描述，日志展示用
     */
    private final String description;

    /**
     * XXL-Job handler 名称，冗余存到 job_record 表方便数据库直接看
     */
    private final String handler;

    /**
     * 前置任务列表。空列表表示无依赖；聚合必须同时满足快照及对应上游成功。
     */
    private final List<AnomalyJobTypeEnum> predecessors;

    /**
     * 初始化任务标识和前置依赖，显式声明以避免依赖 Lombok 注解处理。
     *
     * @param type 任务类型值
     * @param description 任务描述
     * @param handler XXL-Job handler 名称
     * @param predecessors 所有前置任务，无依赖时不传
     */
    AnomalyJobTypeEnum(Integer type, String description, String handler, AnomalyJobTypeEnum... predecessors) {
        this.type = type;
        this.description = description;
        this.handler = handler;
        this.predecessors = List.of(predecessors); // 不可变列表，避免依赖在运行期间被篡改。
    }

    /** @return 存入数据库的任务类型值 */
    public Integer getType() {
        return type;
    }

    /** @return 日志展示用的任务描述 */
    public String getDescription() {
        return description;
    }

    /** @return XXL-Job handler 名称 */
    public String getHandler() {
        return handler;
    }

    /** @return 全部前置任务，无依赖时为空列表 */
    public List<AnomalyJobTypeEnum> getPredecessors() {
        return predecessors;
    }

    /**
     * 按 type 值反查枚举。
     *
     * @param type 任务类型值，允许为 null
     * @return 对应枚举；入参为 null 或类型不存在时返回 null
     */
    public static AnomalyJobTypeEnum valueOfType(Integer type) {
        // 直接遍历枚举常量，匹配稳定的数据库类型值。
        for (AnomalyJobTypeEnum jobType : values()) {
            if (jobType.type.equals(type)) {
                return jobType;
            }
        }
        return null;
    }

    /**
     * 是否月度任务（包含上游月度数据任务），用于执行记录的周期类型
     */
    public boolean isMonthly() {
        return this == SNAPSHOT_MONTH || this == REPORT_MONTH_SALES || this == REPORT_MONTH_ZERO
                || this == PUSH_MONTH_SALES || this == PUSH_MONTH_ZERO
                || this == SOURCE_MONTH_PRODUCT_SALES || this == SOURCE_MONTH_ZERO_ORDER;
    }
}
