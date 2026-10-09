CREATE TABLE `coupon_exchange_commodity` (
     `id` bigint(20) NOT NULL COMMENT 'id',
     `coupon_id` bigint(20) NOT NULL COMMENT '优惠券id',
     `commodity_id` bigint(20) NOT NULL COMMENT '商品id',
     `commodity_name` varchar(256) NOT NULL COMMENT '商品名称',
     `business_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '业务线ID',
     `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='兑换券下单时需要买的商品';


ALTER TABLE `good_coupon`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';

ALTER TABLE `user_coupon`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_0`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_1`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_2`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_3`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_4`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_5`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_6`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_7`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_8`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';
ALTER TABLE `user_coupon_9`
    ADD COLUMN `exchange_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '兑换券是否需要绑定商品 0否 1是';