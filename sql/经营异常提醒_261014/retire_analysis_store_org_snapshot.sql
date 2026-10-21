-- 取消月度门店组织快照后的旧表清理脚本（MySQL）。
-- 执行顺序：先停用/删除 XXL-Job 的 analysisStoreOrgSnapshotMonthJobHandler，
-- 再部署实时统计版本；确认不再需要旧组织快照数据后执行本脚本。
-- 只删除月度门店组织快照表，不影响 analysis_user_store_snapshot 人店关系快照。
DROP TABLE IF EXISTS `analysis_store_org_snapshot`;
