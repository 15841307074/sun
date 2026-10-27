-- 抽奖中奖记录缺失 is_guarantees 字段的单独修复脚本
-- 兼容 MySQL 5.7。仅在已核对这11张表均缺少该字段的实际物理库执行一次。
-- 不切换数据库；不修改中奖记录、不清空数据、不恢复发奖任务。
-- 旧记录保持 NULL，表示历史兜底状态未知；不将历史记录统一设为0。
SET NAMES utf8mb4;
SELECT DATABASE() AS current_database;

ALTER TABLE `lottery_log`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_0`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_1`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_2`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_3`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_4`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_5`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_6`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_7`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_8`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

ALTER TABLE `lottery_log_9`
  ADD COLUMN `is_guarantees` TINYINT NULL DEFAULT NULL COMMENT '奖品兜底快照：0非兜底，1兜底，NULL表示历史状态未知';

-- 执行后核对：应返回11张表，每张表 is_guarantees 存在且允许NULL。
SELECT t.TABLE_SCHEMA AS database_name, t.TABLE_NAME AS table_name,
       IF(c.COLUMN_NAME IS NULL, '缺少', '存在') AS field_status,
       c.COLUMN_TYPE, c.IS_NULLABLE, c.COLUMN_DEFAULT
FROM information_schema.TABLES t
LEFT JOIN information_schema.COLUMNS c
  ON c.TABLE_SCHEMA = t.TABLE_SCHEMA AND c.TABLE_NAME = t.TABLE_NAME
 AND c.COLUMN_NAME = 'is_guarantees'
WHERE t.TABLE_SCHEMA = DATABASE()
  AND (t.TABLE_NAME = 'lottery_log' OR t.TABLE_NAME REGEXP '^lottery_log_[0-9]$')
ORDER BY t.TABLE_NAME;
