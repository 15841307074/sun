ALTER TABLE `coupon_package`
    ADD COLUMN `claim_time_limit` tinyint(1) NOT NULL DEFAULT '0' COMMENT '领取时间限制 0 不限制 1 限制时间' AFTER `community_flag`,
    ADD COLUMN `claim_time_slot` varchar(30) DEFAULT NULL COMMENT '领取时间 指定日期段 年月日#年月日' AFTER `claim_time_limit`,
    ADD COLUMN `claim_day_no` varchar(128) DEFAULT NULL COMMENT '领取时间指定几号 1#2#6' AFTER `claim_time_slot`,
    ADD COLUMN `claim_week_no` varchar(32) DEFAULT NULL COMMENT '领取时间指定周几 1#3' AFTER `claim_day_no`,
    ADD COLUMN `claim_time` varchar(32) DEFAULT NULL COMMENT '领取时间指定时间 时分秒#时分秒' AFTER `claim_week_no`;