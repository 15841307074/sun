-- MySQL 5.7：按分表、按冻结明细修复会员冻结余额
-- 范围：项目 business_id=10，wx_member_0～9 / points_log_0～9，共10个分表。
-- 仅将未删除会员的 freeze_points 赋值为同分表未删除、points_type=4 的明细合计；无明细赋0。
-- 汇总全部冻结明细，不限制48小时；不修改可用积分、累计积分、积分明细或会员等级。
-- 执行前备份会员数据，并暂停积分新增、兑换、解冻及其他会员余额写入。
-- 必须连接真实会员业务库，并确认这两组物理表均为 InnoDB。
-- 【手动分步执行，禁止整份一次运行】每次只选一个分表：步骤1→步骤2→步骤3→步骤4。
-- 一个分表明确 COMMIT 或 ROLLBACK 后，才能开始下一个分表，避免 START TRANSACTION 隐式提交前一事务。
-- 临时表、核对和更新必须在同一个连接执行；临时表断开连接即消失，请先导出保存。
-- 若任何语句报错，立即停止并 ROLLBACK，不要继续提交。
-- 临时表在更新事务之前记录；回滚更新不会清除这份修改前后快照。
-- 未自动删除或覆盖临时表，避免丢失已记录的修改前数据。

-- ==================== 0号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_0 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_0 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_0 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_0
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_0
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_0;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_0 AS m
INNER JOIN tmp_freeze_repair_0 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_0 AS t
LEFT JOIN wx_member_0 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 1号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_1 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_1 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_1 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_1
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_1
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_1;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_1 AS m
INNER JOIN tmp_freeze_repair_1 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_1 AS t
LEFT JOIN wx_member_1 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 2号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_2 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_2 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_2 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_2
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_2
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_2;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_2 AS m
INNER JOIN tmp_freeze_repair_2 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_2 AS t
LEFT JOIN wx_member_2 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 3号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_3 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_3 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_3 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_3
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_3
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_3;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_3 AS m
INNER JOIN tmp_freeze_repair_3 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_3 AS t
LEFT JOIN wx_member_3 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 4号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_4 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_4 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_4 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_4
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_4
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_4;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_4 AS m
INNER JOIN tmp_freeze_repair_4 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_4 AS t
LEFT JOIN wx_member_4 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 5号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_5 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_5 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_5 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_5
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_5
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_5;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_5 AS m
INNER JOIN tmp_freeze_repair_5 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_5 AS t
LEFT JOIN wx_member_5 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 6号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_6 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_6 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_6 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_6
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_6
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_6;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_6 AS m
INNER JOIN tmp_freeze_repair_6 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_6 AS t
LEFT JOIN wx_member_6 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 7号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_7 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_7 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_7 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_7
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_7
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_7;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_7 AS m
INNER JOIN tmp_freeze_repair_7 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_7 AS t
LEFT JOIN wx_member_7 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 8号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_8 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_8 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_8 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_8
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_8
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_8;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_8 AS m
INNER JOIN tmp_freeze_repair_8 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_8 AS t
LEFT JOIN wx_member_8 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。

-- ==================== 9号分表 ====================
-- 步骤1：计算并记录差异（先执行本段，不会修改会员余额）。
CREATE TEMPORARY TABLE tmp_freeze_repair_9 (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_freeze_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分',
    after_freeze_points BIGINT NOT NULL COMMENT '冻结明细合计，即目标冻结积分',
    calculate_time DATETIME NOT NULL COMMENT '计算记录时间',
    PRIMARY KEY (business_id, member_id)
) ENGINE=InnoDB;

INSERT INTO tmp_freeze_repair_9 (
    business_id, member_id, before_freeze_points, after_freeze_points, calculate_time
)
SELECT m.business_id, m.member_id, m.freeze_points, IFNULL(p.frozen_total, 0), NOW()
FROM wx_member_9 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS frozen_total
    FROM points_log_9
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.frozen_total, 0));

-- 步骤2：查看、导出修改前后明细及预计修改人数。
SELECT business_id, member_id, before_freeze_points, after_freeze_points,
       CAST(after_freeze_points AS DECIMAL(65,0))
         - CAST(IFNULL(before_freeze_points,0) AS DECIMAL(65,0)) AS change_points,
       calculate_time
FROM tmp_freeze_repair_9
ORDER BY member_id;

SELECT COUNT(*) AS expected_user_count FROM tmp_freeze_repair_9;

-- 步骤3：核对快照后，仅执行本段更新和检查，暂不提交。
START TRANSACTION;

UPDATE wx_member_9 AS m
INNER JOIN tmp_freeze_repair_9 AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id
SET m.freeze_points=t.after_freeze_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_freeze_points);

-- 必须紧接UPDATE执行；实际修改人数应等于步骤2的预计修改人数。
SELECT ROW_COUNT() AS actual_updated_user_count;

-- 必须返回0：快照中的会员均存在、未删除且冻结余额已达到目标值。
SELECT COUNT(*) AS remaining_mismatch_user_count
FROM tmp_freeze_repair_9 AS t
LEFT JOIN wx_member_9 AS m
    ON m.business_id=t.business_id AND m.member_id=t.member_id AND m.deleted=0
WHERE m.member_id IS NULL
   OR NOT (m.freeze_points <=> t.after_freeze_points);

-- 步骤4：无报错、实际人数等于预计人数且 remaining_mismatch_user_count=0，单独执行：
-- COMMIT;
-- 如报错、人数不符或存在其他异常，单独执行：
-- ROLLBACK;
-- 本分表提交或回滚后，再处理下一分表；临时表仍保留，连接断开前导出保存。
