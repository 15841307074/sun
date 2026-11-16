-- MySQL 5.7/8.0。项目 10，分表 0 到 9 的可用积分修复。执行前选择目标会员库。
-- 全部执行会先准备留档表，然后按 0 到 9 的顺序逐表修复。每个分表整表统计、独立提交。
-- 也可先执行一次建表区段，再单独选择一个分表从“修复开始”到“修复结束”执行。
-- 使用专用查询连接。修复期间暂停项目 10 相关分表的积分写入。
-- COMMITTED 表示该分表已提交。ROLLED_BACK 时查看 check_result。
-- 若后续分表报错或断开，已提交的分表仍然生效；可根据结果从对应区段继续。
--
-- ===== 留档建表开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
-- 在会员库首次执行一次。用独立连接建表，避免 DDL 提交其他事务。
CREATE TABLE IF NOT EXISTS `points_available_reconcile_audit` (
    `repair_no` char(36) NOT NULL COMMENT '本次修复编号',
    `business_id` bigint NOT NULL COMMENT '项目ID',
    `member_id` bigint NOT NULL COMMENT '会员ID',
    `shard_no` tinyint NOT NULL COMMENT '分表编号',
    `repair_action` varchar(20) NOT NULL COMMENT '补流水或补会员余额',
    `available_before` int NOT NULL COMMENT '修复前会员可用积分',
    `ledger_before` decimal(65,0) NOT NULL COMMENT '修复前积分流水合计，不含冻结',
    `target_points` int NOT NULL COMMENT '两边预计修复后的积分',
    `available_after` int DEFAULT NULL COMMENT '修复后实际会员可用积分',
    `ledger_after` decimal(65,0) DEFAULT NULL COMMENT '修复后实际积分流水合计，不含冻结',
    `inserted_points_log_id` bigint DEFAULT NULL COMMENT '本次补写的积分流水主键',
    `check_result` varchar(24) NOT NULL COMMENT '修复校验结果',
    `created_at` datetime NOT NULL COMMENT '修改前留档时间',
    `checked_at` datetime DEFAULT NULL COMMENT '修复后校验时间',
    PRIMARY KEY (`repair_no`),
    KEY `idx_member_created_at` (`business_id`, `member_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员可用积分对账修复留档';
-- ===== 留档建表结束 =====

-- ===== 分表 0 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 0, plan.repair_action,
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
    FROM wx_member_0 m
    LEFT JOIN points_log_0 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 0 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 0 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 0 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 0
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 0
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_0 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 0
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 0 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_0 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 0, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_0 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 0
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 0 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_0 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 0
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 0 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_0 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_0 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 0 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_0 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 0 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 0
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 0 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 0
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 0
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 0 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 0 修复结束 =====

-- ===== 分表 1 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

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

SELECT 1 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 1
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

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

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 1 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 1 修复结束 =====

-- ===== 分表 2 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 2, plan.repair_action,
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
    FROM wx_member_2 m
    LEFT JOIN points_log_2 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 2 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 2 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 2 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 2
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 2
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_2 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 2
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 2 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_2 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 2, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_2 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 2
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 2 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_2 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 2
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 2 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_2 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_2 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 2 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_2 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 2 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 2
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 2 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 2
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 2
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 2 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 2 修复结束 =====

-- ===== 分表 3 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 3, plan.repair_action,
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
    FROM wx_member_3 m
    LEFT JOIN points_log_3 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 3 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 3 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 3 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 3
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 3
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_3 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 3
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 3 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_3 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 3, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_3 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 3
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 3 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_3 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 3
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 3 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_3 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_3 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 3 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_3 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 3 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 3
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 3 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 3
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 3
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 3 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 3 修复结束 =====

-- ===== 分表 4 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 4, plan.repair_action,
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
    FROM wx_member_4 m
    LEFT JOIN points_log_4 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 4 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 4 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 4 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 4
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 4
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_4 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 4
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 4 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_4 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 4, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_4 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 4
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 4 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_4 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 4
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 4 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_4 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_4 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 4 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_4 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 4 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 4
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 4 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 4
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 4
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 4 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 4 修复结束 =====

-- ===== 分表 5 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 5, plan.repair_action,
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
    FROM wx_member_5 m
    LEFT JOIN points_log_5 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 5 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 5 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 5 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 5
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 5
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_5 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 5
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 5 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_5 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 5, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_5 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 5
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 5 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_5 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 5
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 5 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_5 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_5 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 5 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_5 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 5 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 5
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 5 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 5
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 5
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 5 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 5 修复结束 =====

-- ===== 分表 6 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 6, plan.repair_action,
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
    FROM wx_member_6 m
    LEFT JOIN points_log_6 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 6 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 6 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 6 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 6
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 6
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_6 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 6
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 6 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_6 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 6, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_6 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 6
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 6 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_6 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 6
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 6 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_6 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_6 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 6 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_6 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 6 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 6
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 6 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 6
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 6
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 6 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 6 修复结束 =====

-- ===== 分表 7 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 7, plan.repair_action,
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
    FROM wx_member_7 m
    LEFT JOIN points_log_7 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 7 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 7 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 7 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 7
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 7
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_7 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 7
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 7 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_7 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 7, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_7 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 7
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 7 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_7 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 7
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 7 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_7 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_7 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 7 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_7 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 7 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 7
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 7 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 7
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 7
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 7 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 7 修复结束 =====

-- ===== 分表 8 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 8, plan.repair_action,
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
    FROM wx_member_8 m
    LEFT JOIN points_log_8 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 8 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 8 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 8 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 8
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 8
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_8 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 8
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 8 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_8 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 8, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_8 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 8
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 8 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_8 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 8
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 8 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_8 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_8 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 8 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_8 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 8 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 8
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 8 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 8
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 8
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 8 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 8 修复结束 =====

-- ===== 分表 9 修复开始 =====
ROLLBACK AND NO CHAIN NO RELEASE;
SET @business_id = 10;
SET @repair_time = NOW();
-- 同一轮共用编号前缀，后 16 位为会员 ID 的十六进制值。范围查询走留档表主键。
SET @repair_prefix = CONCAT(LEFT(SHA2(UUID(), 256), 19), '-');
SET @repair_first = CONCAT(@repair_prefix, '0000000000000000');
SET @repair_last = CONCAT(@repair_prefix, 'FFFFFFFFFFFFFFFF');
SET @archived_rows = -1, @out_of_range_rows = -1, @planned_rows = -1,
    @locked_rows = -1, @inserted_rows = -1, @updated_rows = -1, @verified_rows = -1;

SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;

-- 1. 统计并留档。流水按正负值求和，排除未转为可用的冻结类型 4，包含过期流水。
INSERT INTO points_available_reconcile_audit (
    repair_no, business_id, member_id, shard_no, repair_action,
    available_before, ledger_before, target_points, check_result, created_at
)
SELECT CONCAT(@repair_prefix, LPAD(HEX(plan.member_id), 16, '0')),
       @business_id, plan.member_id, 9, plan.repair_action,
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
    FROM wx_member_9 m
    LEFT JOIN points_log_9 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 9 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @business_id AND m.sharding_value = 9 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) plan;
SET @archived_rows = ROW_COUNT();

SELECT 9 AS shard_no, repair_action, COUNT(*) AS member_count,
       SUM(ABS(available_before - ledger_before)) AS points_difference
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 9
GROUP BY repair_action;

SELECT COUNT(*) INTO @out_of_range_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 9
  AND repair_action = 'AMOUNT_OUT_OF_RANGE';
SET @planned_rows = IF(@out_of_range_rows >= 0, @archived_rows - @out_of_range_rows, -1);

-- 2. 锁定待修复会员。待修复余额均与留档一致才执行写入。
SELECT COUNT(*) INTO @locked_rows
FROM points_available_reconcile_audit a
JOIN wx_member_9 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 9
  AND a.check_result = 'PENDING'
  AND m.sharding_value = 9 AND m.deleted = b'0' AND m.member_integral = a.available_before
FOR UPDATE;

-- 3. 按留档执行。可用余额较大时补流水，流水合计较大时补余额。
INSERT INTO points_log_9 (
    member_id, business_id, sharding_value, points_change, points_type,
    points_log_status, is_points_product, log_code, product_name, operator,
    member_name, member_nick_name, member_mobile, create_time, update_time, deleted
)
SELECT m.member_id, m.business_id, 9, a.available_before - a.ledger_before, 1,
       1, 2, CONCAT('POINTS-RECONCILE:MEMBER:', m.member_id, ':', a.repair_no),
       '可用积分对账补录', 'manual-reconcile', m.member_name, m.member_nick_name,
       m.member_mobile, '2025-12-31 00:00:00', NOW(), b'0'
FROM points_available_reconcile_audit a
JOIN wx_member_9 m ON m.member_id = a.member_id AND m.business_id = a.business_id
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 9
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'INSERT_LEDGER'
  AND m.sharding_value = 9 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @inserted_rows = ROW_COUNT();

UPDATE points_available_reconcile_audit a
JOIN wx_member_9 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET m.member_integral = a.target_points, m.update_time = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 9
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND a.repair_action = 'UPDATE_MEMBER'
  AND m.sharding_value = 9 AND m.deleted = b'0' AND m.member_integral = a.available_before;
SET @updated_rows = ROW_COUNT();

-- 4. 回填实际余额、实际流水合计和本次补写的流水 ID。
UPDATE points_available_reconcile_audit a
JOIN wx_member_9 m ON m.member_id = a.member_id AND m.business_id = a.business_id
SET a.available_after = m.member_integral,
    a.ledger_after = (
        SELECT COALESCE(SUM(p.points_change), 0)
        FROM points_log_9 p
        WHERE p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 9 AND p.deleted = b'0' AND p.points_type <> 4
    ),
    a.inserted_points_log_id = CASE WHEN a.repair_action = 'INSERT_LEDGER' THEN (
        SELECT MAX(p.points_log_id)
        FROM points_log_9 p
        WHERE p.log_code = CONCAT('POINTS-RECONCILE:MEMBER:', a.member_id, ':', a.repair_no)
          AND p.member_id = a.member_id AND p.business_id = a.business_id
          AND p.sharding_value = 9 AND p.deleted = b'0' AND p.points_type = 1
    ) END,
    a.checked_at = NOW()
WHERE a.repair_no BETWEEN @repair_first AND @repair_last AND a.business_id = @business_id AND a.shard_no = 9
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows
  AND m.sharding_value = 9 AND m.deleted = b'0';

UPDATE points_available_reconcile_audit
SET check_result = CASE
    WHEN available_after = target_points AND ledger_after = target_points
     AND (repair_action <> 'INSERT_LEDGER' OR inserted_points_log_id IS NOT NULL)
        THEN 'MATCHED'
    ELSE 'NOT_MATCHED'
END
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 9
  AND @planned_rows >= 0 AND @locked_rows = @planned_rows AND check_result = 'PENDING';

SELECT COUNT(*) INTO @verified_rows
FROM points_available_reconcile_audit
WHERE repair_no BETWEEN @repair_first AND @repair_last AND business_id = @business_id AND shard_no = 9
  AND check_result = 'MATCHED';

SET @repair_check_result = CASE
    WHEN @business_id IS NULL OR @repair_time IS NULL OR @repair_prefix IS NULL
      OR @repair_first IS NULL OR @repair_last IS NULL THEN 'NOT_INITIALIZED'
    WHEN @planned_rows IS NULL OR @planned_rows < 0 OR @locked_rows IS NULL
      OR @locked_rows <> @planned_rows THEN 'NOT_READY'
    WHEN @inserted_rows IS NULL OR @updated_rows IS NULL OR @verified_rows IS NULL
      OR @inserted_rows < 0 OR @updated_rows < 0 OR @inserted_rows + @updated_rows <> @planned_rows
      OR @verified_rows <> @planned_rows THEN 'NOT_MATCHED'
    WHEN @out_of_range_rows > 0 THEN 'AMOUNT_OUT_OF_RANGE'
    WHEN @planned_rows = 0 THEN 'NO_CHANGES'
    ELSE 'MATCHED'
END;

-- 校验通过才提交。其余结果通过下面的 ROLLBACK 结束本轮事务。
SET @repair_finish_sql = IF(@repair_check_result = 'MATCHED', 'COMMIT AND NO CHAIN NO RELEASE', 'DO 0');
PREPARE points_repair_finish FROM @repair_finish_sql;
EXECUTE points_repair_finish;
SET @repair_finish_rows = ROW_COUNT();
DEALLOCATE PREPARE points_repair_finish;
ROLLBACK AND NO CHAIN NO RELEASE;
SET @repair_rollback_rows = ROW_COUNT();

-- 本分表的汇总结果在事务结束后显示。若客户端因报错提前停止且未显示结果 2，执行 ROLLBACK 后再排查。
SELECT @business_id AS business_id, 9 AS shard_no, @repair_prefix AS repair_prefix, DATABASE() AS database_name,
       @planned_rows AS planned_rows, @archived_rows AS archived_rows, @locked_rows AS locked_rows,
       @out_of_range_rows AS out_of_range_rows,
       @inserted_rows AS inserted_ledger_rows, @updated_rows AS updated_member_rows, @verified_rows AS verified_rows,
       @repair_check_result AS check_result,
       CASE
           WHEN NOT (@repair_finish_rows <=> 0 AND @repair_rollback_rows <=> 0) THEN 'FINALIZE_FAILED'
           WHEN @repair_check_result = 'MATCHED' THEN 'COMMITTED'
           ELSE 'ROLLED_BACK'
       END AS transaction_result;
-- ===== 分表 9 修复结束 =====
