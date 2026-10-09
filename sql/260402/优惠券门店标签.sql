ALTER TABLE `coupon_store`
    ADD COLUMN `tag_id` bigint(20) DEFAULT NULL COMMENT '门店标签id';

UPDATE `good_coupon` SET `instructions` = 1;


ALTER TABLE `good_coupon`
    CHANGE COLUMN `instructions` `store_tag_flag` tinyint(4) DEFAULT '1' COMMENT '绑定门店标签 0 绑定 1 不绑定';


