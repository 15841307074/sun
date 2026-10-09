ALTER TABLE `activity_cq_log`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_0`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_1`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_2`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_3`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_4`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_5`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_6`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_7`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_8`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);

ALTER TABLE `activity_cq_log_9`
    ADD COLUMN `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女' AFTER `member_mobile`,
    ADD COLUMN `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝' AFTER `gender`,
    ADD KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`);
