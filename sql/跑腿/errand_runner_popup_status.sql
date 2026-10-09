ALTER TABLE `bz_errand_runner`
  ADD COLUMN `popup_status` tinyint NOT NULL DEFAULT 0 COMMENT '是否已弹窗 0未弹窗 1已弹窗' AFTER `audit_status`;
