-- MySQL 5.7/8.0。默认一次修复当前会员库内项目 10 的 1 号分表。
-- 首次先执行 points_available_reconcile_audit.sql。修复期间暂停本项目该分表的积分写入。
-- 用专用连接从头执行。开头回滚本连接已有的未提交事务，末尾由操作者提交或回滚。
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 1, plan.repair_action,
       plan.available_points, plan.ledger_points,
       CASE WHEN plan.repair_action = 'UPDATE_MEMBER' THEN plan.ledger_points ELSE plan.available_points END,
       CASE WHEN plan.repair_action = 'AMOUNT_OUT_OF_RANGE' THEN 'AMOUNT_OUT_OF_RANGE' ELSE 'PENDING' END,
       @repair_time
FROM (
    SELECT m.member_id, m.member_integral AS available_points,
           COALESCE(SUM(p.points_change), 0) AS ledger_points,
           CASE
               WHEN COALESCE(SUM(p.points_change), 0) > 2147483647
                 OR m.member_integral - COALESCE(SUM(p.points_change), 0) > 9223372036854775807
                   THEN 'AMOUNT_OUT_OF_RANGE'
               WHEN m.member_integral > COALESCE(SUM(p.points_change), 0) THEN 'INSERT_LEDGER'
               ELSE 'UPDATE_MEMBER'
           END AS repair_action
    FROM wx_member_1 m
    LEFT JOIN points_log_1 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 1 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 1 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = @archived_rows - @out_of_range_rows;

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_1 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 1
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 1 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_1 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 1, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_1 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 1
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 1 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_1 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 1
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 1 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_1 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_1 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 1 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_1 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 1 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 1
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 1 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
  AND check_result = 'MATCHED';

SELECT @business_id AS business_id, 1 AS shard_no, @repair_prefix AS repair_prefix,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       CASE
           WHEN @planned_rows < 0 OR @locked_rows <> @planned_rows THEN 'NOT_READY'
           WHEN @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
             OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
           WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
           WHEN @planned_rows = 0 THEN 'NO_CHANGES'
           ELSE 'MATCHED'
       END AS check_result;

-- 看到 MATCHED 可提交。AMOUNT_OUT_OF_RANGE 表示有记录超出字段范围，需复核留档后决定。
-- 有报错或结果为 NOT_READY / NOT_MATCHED 时回滚。NO_CHANGES 可直接回滚结束事务。
-- COMMIT AND NO CHAIN NO RELEASE;
-- ROLLBACK AND NO CHAIN NO RELEASE;
