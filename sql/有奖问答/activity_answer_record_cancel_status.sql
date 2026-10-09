-- 有奖问答取消答题标识。
-- 分表环境执行 activity_answer_record_0 至 activity_answer_record_9；
-- 未分表环境执行最后一条 activity_answer_record。

ALTER TABLE `activity_answer_record_0` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_1` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_2` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_3` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_4` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_5` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_6` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_7` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_8` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
ALTER TABLE `activity_answer_record_9` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;

-- 未分表环境按需执行：
-- ALTER TABLE `activity_answer_record` ADD COLUMN `cancel_status` int(1) NOT NULL DEFAULT '0' COMMENT '取消状态 0未取消 1已取消' AFTER `status`;
