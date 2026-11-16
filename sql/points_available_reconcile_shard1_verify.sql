-- 独立复核项目 10 的 1 号分表。可以在原连接提交前运行，或在其他连接提交后运行。
SET @verify_business_id = 10;

-- 1. 统计仍有差额的会员数。
SELECT @verify_business_id AS business_id, 1 AS shard_no,
       COUNT(*) AS remaining_mismatch_members,
       COALESCE(SUM(diff.available_points > diff.ledger_points), 0) AS member_balance_higher,
       COALESCE(SUM(diff.available_points < diff.ledger_points), 0) AS ledger_higher
FROM (
    SELECT m.member_id, m.member_integral AS available_points, COALESCE(SUM(p.points_change), 0) AS ledger_points
    FROM wx_member_1 m
    LEFT JOIN points_log_1 p
           ON p.member_id = m.member_id AND p.business_id = m.business_id
          AND p.sharding_value = 1 AND p.deleted = b'0' AND p.points_type <> 4
    WHERE m.business_id = @verify_business_id AND m.sharding_value = 1 AND m.deleted = b'0'
    GROUP BY m.member_id, m.member_integral
    HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
) diff;

-- 2. 列出仍有差额的会员及差值。
SELECT m.member_id, m.member_integral AS `会员可用积分`,
       COALESCE(SUM(p.points_change), 0) AS `积分记录总和（不含冻结）`,
       m.member_integral - COALESCE(SUM(p.points_change), 0) AS `差值（可用积分减流水）`
FROM wx_member_1 m
LEFT JOIN points_log_1 p
       ON p.member_id = m.member_id AND p.business_id = m.business_id
      AND p.sharding_value = 1 AND p.deleted = b'0' AND p.points_type <> 4
WHERE m.business_id = @verify_business_id AND m.sharding_value = 1 AND m.deleted = b'0'
GROUP BY m.member_id, m.member_integral
HAVING m.member_integral <> COALESCE(SUM(p.points_change), 0)
ORDER BY m.member_id;

-- 3. 显示最近 100 条留档。
SELECT repair_no, member_id, repair_action,
       available_before AS `修复前可用积分`, ledger_before AS `修复前积分记录总和`,
       available_after AS `修复后可用积分`, ledger_after AS `修复后积分记录总和`,
       inserted_points_log_id, check_result, created_at, checked_at
FROM points_available_reconcile_audit
WHERE business_id = @verify_business_id AND shard_no = 1
ORDER BY created_at DESC, repair_no DESC
LIMIT 100;
