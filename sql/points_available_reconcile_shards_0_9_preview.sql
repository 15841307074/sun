-- 项目 10，分表 0 到 9 的修复前只读查询。执行前在 Navicat 选择目标会员库。
-- 全选会按分表顺序查询首页，也可单独选择一个分表从“查询开始”到“查询结束”执行。
-- 每个分表每页先取最多 1000 个会员再汇总流水。查下一页时，把该区段的
-- member_id > 0 中的 0 改为当前页最后一行的 member_id，再运行该区段。
-- ALREADY_MATCHED 表示该会员无差额。过期流水参与汇总，冻结积分单独展示。

-- ===== 分表 0 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 0 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_0 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 0 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_0 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 0 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 0 查询结束 =====

-- ===== 分表 1 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 1 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_1 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 1 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_1 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 1 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 1 查询结束 =====

-- ===== 分表 2 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 2 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_2 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 2 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_2 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 2 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 2 查询结束 =====

-- ===== 分表 3 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 3 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_3 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 3 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_3 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 3 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 3 查询结束 =====

-- ===== 分表 4 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 4 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_4 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 4 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_4 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 4 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 4 查询结束 =====

-- ===== 分表 5 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 5 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_5 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 5 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_5 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 5 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 5 查询结束 =====

-- ===== 分表 6 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 6 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_6 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 6 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_6 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 6 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 6 查询结束 =====

-- ===== 分表 7 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 7 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_7 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 7 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_7 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 7 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 7 查询结束 =====

-- ===== 分表 8 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 8 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_8 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 8 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_8 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 8 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 8 查询结束 =====

-- ===== 分表 9 查询开始 =====
SELECT /*+ MAX_EXECUTION_TIME(45000) */ 9 AS shard_no, diff.member_id, diff.business_id,
       diff.available_points AS `会员可用积分`,
       diff.ledger_points AS `积分记录总和（不含冻结）`,
       diff.available_points - diff.ledger_points AS `差值（可用积分减流水）`,
       diff.frozen_ledger_points AS `冻结积分流水汇总`,
       diff.member_frozen_points AS `会员冻结积分`,
       CASE
           WHEN diff.available_points = diff.ledger_points THEN 'ALREADY_MATCHED'
           WHEN diff.ledger_points > 2147483647
             OR diff.available_points - diff.ledger_points > 9223372036854775807 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN diff.available_points > diff.ledger_points THEN 'INSERT_LEDGER'
           ELSE 'UPDATE_MEMBER'
       END AS repair_action
FROM (
    SELECT m.member_id, m.business_id, m.member_integral AS available_points,
           COALESCE(SUM(CASE WHEN p.points_type <> 4 THEN p.points_change END), 0) AS ledger_points,
           COALESCE(SUM(CASE WHEN p.points_type = 4 THEN p.points_change END), 0) AS frozen_ledger_points,
           m.freeze_points AS member_frozen_points
    FROM (
        SELECT member_id, business_id, member_integral, freeze_points
        FROM wx_member_9 FORCE INDEX (PRIMARY)
        WHERE member_id > 0 AND business_id = 10 AND sharding_value = 9 AND deleted = b'0'
        ORDER BY member_id
        LIMIT 1000
    ) m
    LEFT JOIN points_log_9 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 9 AND p.deleted = b'0'
    GROUP BY m.member_id, m.business_id, m.member_integral, m.freeze_points
) diff
ORDER BY diff.member_id;
-- ===== 分表 9 查询结束 =====
