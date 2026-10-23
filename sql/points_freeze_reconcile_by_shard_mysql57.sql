-- MySQL 5.7：项目10，0～9分表分别修复冻结积分，共用一张临时表记录修改前后值。
-- 执行顺序：初始化临时表一次；然后依次执行每个分表的“1留档→2更新→3重新对账”。
-- 只记录、修改不一致会员；无冻结明细目标为0；NULL与0视为不一致。
-- 明细口径：同分表business_id=10、deleted=0、points_type=4全部明细，不限制48小时。
-- 会员范围：business_id=10、deleted=0；只修改freeze_points。
-- 执行前备份会员表，暂停积分新增、兑换、解冻及其他会员余额写入，避免计算后数据又变化。
-- 使用开启自动提交、没有其他未提交事务的独立连接；全程同一连接，临时表断开后消失，先导出再断开。
-- 按分表分步执行；任何SQL报错立即停止，不要跳过错误继续执行。
-- 留档后如需重新执行本分表，只重跑“2更新、3对账”，不要重复插入或清空修改前的记录。
-- UPDATE只匹配仍等于修改前余额的会员；余额已变化会跳过，由重新对账显示剩余不一致人数。

-- ==================== 初始化：只执行一次 ====================
CREATE TEMPORARY TABLE tmp_freeze_points_change (
    business_id BIGINT NOT NULL COMMENT '项目ID',
    shard_no TINYINT NOT NULL COMMENT '分表编号0至9',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    before_points BIGINT DEFAULT NULL COMMENT '修改前冻结积分，保留原始NULL',
    after_points BIGINT NOT NULL COMMENT '修改目标冻结积分，即冻结明细合计',
    calculate_time DATETIME NOT NULL COMMENT '计算留档时间',
    PRIMARY KEY (business_id, shard_no, member_id)
) ENGINE=InnoDB;

-- ==================== 0号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 0, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_0 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_0
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 0 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=0;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_0 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=0
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 0 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_0 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_0
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 1号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 1, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_1 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_1
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 1 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=1;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_1 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=1
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 1 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_1 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_1
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 2号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 2, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_2 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_2
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 2 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=2;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_2 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=2
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 2 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_2 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_2
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 3号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 3, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_3 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_3
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 3 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=3;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_3 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=3
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 3 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_3 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_3
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 4号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 4, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_4 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_4
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 4 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=4;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_4 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=4
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 4 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_4 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_4
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 5号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 5, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_5 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_5
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 5 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=5;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_5 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=5
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 5 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_5 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_5
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 6号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 6, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_6 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_6
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 6 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=6;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_6 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=6
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 6 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_6 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_6
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 7号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 7, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_7 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_7
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 7 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=7;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_7 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=7
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 7 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_7 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_7
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 8号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 8, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_8 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_8
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 8 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=8;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_8 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=8
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 8 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_8 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_8
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 9号分表 ====================
-- 1. 先把不一致会员的修改前后数值存到公共临时表。
INSERT INTO tmp_freeze_points_change
    (business_id, shard_no, member_id, before_points, after_points, calculate_time)
SELECT m.business_id, 9, m.member_id, m.freeze_points, IFNULL(p.total,0), NOW()
FROM wx_member_9 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_9
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- 查看本分表预计修改人数（0表示没有差异，更新不会修改任何会员）。
SELECT 9 AS 分表编号, COUNT(*) AS 预计修改人数
FROM tmp_freeze_points_change WHERE business_id=10 AND shard_no=9;

-- 2. 按临时表中的目标值更新，只修改冻结积分。
UPDATE wx_member_9 AS m
INNER JOIN tmp_freeze_points_change AS t
    ON t.business_id=m.business_id AND t.member_id=m.member_id AND t.shard_no=9
SET m.freeze_points=t.after_points
WHERE m.business_id=10 AND m.deleted=0
  AND (m.freeze_points <=> t.before_points)
  AND NOT (m.freeze_points <=> t.after_points);

-- 紧接UPDATE执行，查看实际修改人数。
SELECT ROW_COUNT() AS 实际修改人数;

-- 3. 重新从真实明细计算，验证本分表是否还有不一致；应为0。
SELECT 9 AS 分表编号, COUNT(*) AS 剩余不一致人数
FROM wx_member_9 AS m
LEFT JOIN (
    SELECT business_id, member_id, SUM(points_change) AS total
    FROM points_log_9
    WHERE business_id=10 AND deleted=0 AND points_type=4
    GROUP BY business_id, member_id
) AS p ON p.business_id=m.business_id AND p.member_id=m.member_id
WHERE m.business_id=10 AND m.deleted=0
  AND NOT (m.freeze_points <=> IFNULL(p.total,0));

-- ==================== 最后：统一查看修改前后记录 ====================
-- 各分表留档人数（只是留档计数，不代表实际更新成功人数；以每表UPDATE结果及重新对账为准）。
SELECT shard_no AS 分表编号, COUNT(*) AS 留档人数
FROM tmp_freeze_points_change WHERE business_id=10
GROUP BY shard_no ORDER BY shard_no;

-- 预览前200条；需要导出全部记录时，去掉LIMIT 200。
SELECT shard_no AS 分表编号, member_id AS 会员ID,
       before_points AS 修改前冻结积分, after_points AS 修改目标冻结积分,
       CAST(after_points AS DECIMAL(65,0))-CAST(IFNULL(before_points,0) AS DECIMAL(65,0)) AS 变化积分,
       calculate_time AS 计算时间
FROM tmp_freeze_points_change WHERE business_id=10
ORDER BY shard_no, member_id LIMIT 200;

-- 各分表重新对账均为0、留档导出保存后，再恢复积分相关写入及解冻任务。
