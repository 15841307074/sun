-- 修复前只读预览项目 10 的 1 号分表。每次最多核对 1000 个会员。
-- 首次保留 member_id > 0。继续查询时，把 0 换成这一页最后一行的 member_id。
-- ALREADY_MATCHED 表示该会员无差额。过期流水参与汇总，冻结积分单独展示。
SELECT /*+ MAX_EXECUTION_TIME(45000) */ diff.member_id, diff.business_id,
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
