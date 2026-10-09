ALTER TABLE `good_coupon` CHANGE COLUMN `duration` `coupon_name_color` varchar(32) DEFAULT NULL COMMENT '优惠券名字的颜色';
ALTER TABLE `good_coupon` CHANGE COLUMN `coupon_status` `name_concatenation` tinyint(4) DEFAULT '0' COMMENT '名字拼接,0拼 1不拼';
UPDATE `good_coupon` SET `name_concatenation` = 0;
UPDATE `good_coupon` SET `coupon_name_color` = 'rgb(249, 133, 25)';