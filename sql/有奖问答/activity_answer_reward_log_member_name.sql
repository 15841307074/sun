-- 有奖问答发放记录增加会员昵称字段。
-- 发放记录按 member_mobile 尾号分为 10 张表，需要全部执行。

ALTER TABLE `activity_answer_reward_log`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_1`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_2`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_3`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_4`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_5`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_6`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_7`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_8`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
ALTER TABLE `activity_answer_reward_log_9`
    ADD COLUMN `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称' AFTER `member_id`;
