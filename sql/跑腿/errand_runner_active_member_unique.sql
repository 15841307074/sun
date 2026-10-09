-- 跑腿员 member_id 唯一索引调整
-- 目标：
-- 1. 允许 deleted = 1 的历史跑腿员记录重新入驻。
-- 2. 保留 deleted = 0 的未删除跑腿员记录按 member_id 唯一，避免并发重复申请产生多条有效骑手。
--
-- 说明：
-- MySQL 普通唯一索引 uk_member_id(member_id) 会拦截已删除数据重新添加。
-- 这里通过生成列 active_member_id 实现“只对未删除数据唯一”：
-- deleted = 0 时 active_member_id = member_id，会受唯一索引约束；
-- deleted = 1 时 active_member_id = NULL，唯一索引允许多个 NULL，不影响历史删除记录。

ALTER TABLE `bz_errand_runner`
  DROP INDEX `uk_member_id`;

ALTER TABLE `bz_errand_runner`
  ADD COLUMN `active_member_id` bigint
    GENERATED ALWAYS AS (
      CASE WHEN `deleted` = b'0' THEN `member_id` ELSE NULL END
    ) STORED COMMENT '未删除骑手会员ID，用于唯一约束' AFTER `member_id`;

ALTER TABLE `bz_errand_runner`
  ADD UNIQUE KEY `uk_active_member_id` (`active_member_id`);

ALTER TABLE `bz_errand_runner`
  ADD INDEX `idx_member_id` (`member_id`);
