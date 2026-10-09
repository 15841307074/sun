-- 有奖问答奖励配置表字段调整
-- prize_id 调整为 award_id，并补充编码、优惠券名称字段

ALTER TABLE `activity_answer_reward`
  CHANGE COLUMN `prize_id` `award_id` bigint(20) DEFAULT NULL COMMENT '奖品id(根据类型判断是优惠卷 id 还是商品 id)',
  ADD COLUMN `code` varchar(255) DEFAULT NULL COMMENT '编码' AFTER `award_id`,
  ADD COLUMN `coupon_name` varchar(255) DEFAULT NULL COMMENT '优惠卷名称' AFTER `code`;

DROP INDEX `idx_prize` ON `activity_answer_reward`;

CREATE INDEX `idx_prize` ON `activity_answer_reward` (`prize_type`, `award_id`);
