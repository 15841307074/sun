-- 补齐门店费用关系表的校园配送费用默认数据
-- 说明：
-- 1. 仅针对已经存在 0/1/2 三类未删除费用、但缺少 3=校园配送费用的门店补充一条默认数据。
-- 2. 脚本可重复执行，已存在 type=3 的门店不会重复插入。
-- 3. type=3 默认代取起送费、配送费均为 0，创建人和修改人为 admin。
-- 4. tenant_id 不从历史费用数据取值，使用表字段默认值 0。

INSERT INTO `system_store_expenses` (
    `store_id`,
    `store_expenses_type`,
    `minimum_delivery_fee`,
    `additionaa_costs`,
    `store_calculation_type`,
    `creator`,
    `create_time`,
    `updater`,
    `update_time`,
    `deleted`,
    `business_id`
)
SELECT
    source_store.`store_id`,
    3 AS `store_expenses_type`,
    0.00 AS `minimum_delivery_fee`,
    0.00 AS `additionaa_costs`,
    0 AS `store_calculation_type`,
    'admin' AS `creator`,
    NOW() AS `create_time`,
    'admin' AS `updater`,
    NOW() AS `update_time`,
    b'0' AS `deleted`,
    source_store.`business_id`
FROM (
    SELECT
        `store_id`,
        MAX(`business_id`) AS `business_id`
    FROM `system_store_expenses`
    WHERE `deleted` = b'0'
      AND `store_expenses_type` IN (0, 1, 2)
    GROUP BY `store_id`
    HAVING COUNT(DISTINCT `store_expenses_type`) = 3
) source_store
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_store_expenses` existing_expense
    WHERE existing_expense.`store_id` = source_store.`store_id`
      AND existing_expense.`store_expenses_type` = 3
      AND existing_expense.`deleted` = b'0'
);
