-- ClickHouse：门店周度异常指标表。
-- 用途：保存各项目、各门店、各 ISO 自然周的异常指标；同一业务键通过 version 保留最新版本。
-- 执行说明：只创建不存在的表，不删除、不覆盖已有表和数据。

CREATE TABLE IF NOT EXISTS analytics.store_anomaly_metrics_weekly
(
    `id` UInt64 COMMENT '记录ID，由写入任务生成',
    `business_id` UInt64 COMMENT '项目ID，用于不同项目之间的数据隔离',
    `store_id` UInt64 COMMENT '门店ID',
    `store_name` String COMMENT '门店名称，保留统计时的名称快照',
    `stat_year` UInt16 COMMENT 'ISO周所属年份，例如跨年周可能不同于自然年',
    `stat_week` UInt8 COMMENT 'ISO周序号，取值1至53',

    `weekly_turnover` Nullable(Decimal(18, 2)) COMMENT '本周营业额异常指标值；为空表示该指标无异常',
    `prev_week_turnover` Nullable(Decimal(18, 2)) COMMENT '上一周营业额；为空表示该指标无异常或无数据',
    `weekly_avg_order_amount_change_rate` Nullable(Decimal(18, 4)) COMMENT '本周客单价变动率；为空表示该指标无异常',
    `weekly_avg_order_amount` Nullable(Decimal(18, 2)) COMMENT '本周客单价；为空表示该指标无异常或无数据',
    `prev_week_avg_order_amount` Nullable(Decimal(18, 2)) COMMENT '上一周客单价；为空表示该指标无异常或无数据',
    `avg_order_amount_change` Nullable(Decimal(18, 2)) COMMENT '客单价变动金额；为空表示该指标无异常',
    `weekly_repeat_customer_count` Nullable(UInt64) COMMENT '本周复购人数；为空表示该指标无异常',
    `weekly_order_customer_count` Nullable(UInt64) COMMENT '本周下单人数；为空表示该指标无异常或无数据',
    `weekly_meal_sales_quantity` Nullable(UInt64) COMMENT '本周餐品销量；为空表示该指标无异常',
    `prev_week_meal_sales_quantity` Nullable(UInt64) COMMENT '上一周餐品销量；为空表示该指标无异常或无数据',

    `complaint_order_ratio` Nullable(Decimal(18, 2)) COMMENT '投诉订单数除以有效订单数乘100后的百分比；有效订单为0时为空',
    `complaint_order_count` Nullable(UInt64) COMMENT '本周去重投诉订单数；无投诉时为空',
    `valid_order_count` Nullable(UInt64) COMMENT '计算投诉占比所用的本周有效订单数；无投诉时为空',
    `food_hygiene_complaint_order_count` Nullable(UInt64) COMMENT '本周食品卫生类去重投诉订单数；无食卫投诉时为空',

    `creator` String DEFAULT '' COMMENT '创建人或创建任务标识',
    `create_time` DateTime64(3) DEFAULT now64(3) COMMENT '首次创建时间',
    `updater` String DEFAULT '' COMMENT '最后更新人或更新任务标识',
    `update_time` DateTime64(3) DEFAULT now64(3) COMMENT '本版本写入时间',
    `deleted` UInt8 DEFAULT 0 COMMENT '逻辑删除标识：0有效，1已删除',
    `version` UInt64 COMMENT '版本号；ReplacingMergeTree按此字段保留业务键的最新版本'
)
ENGINE = ReplacingMergeTree(version)
PARTITION BY stat_year
ORDER BY (business_id, stat_year, stat_week, store_id)
COMMENT '门店周度经营异常指标表，同一项目门店周只保留最新版本';
