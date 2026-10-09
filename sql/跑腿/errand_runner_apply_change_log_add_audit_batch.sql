ALTER TABLE `bz_errand_runner_apply_change_log`
  ADD COLUMN `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2失败' AFTER `member_id`,
  ADD COLUMN `batch_no` varchar(64) NOT NULL COMMENT '修改批次号' AFTER `audit_status`,
  ADD KEY `idx_audit_status` (`audit_status`),
  ADD KEY `idx_batch_no` (`batch_no`);
