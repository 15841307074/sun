-- 门店加盟商身份证照片
ALTER TABLE `system_store_franchisee_info`
    ADD COLUMN `id_card_front_url` varchar(1024) NULL COMMENT '身份证正面图片地址' AFTER `id_card_no`,
    ADD COLUMN `id_card_back_url` varchar(1024) NULL COMMENT '身份证反面图片地址' AFTER `id_card_front_url`;

-- 投诉是否接收回访；历史数据默认按不接收处理
ALTER TABLE `system_complaint`
    ADD COLUMN `accept_follow_up` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否接收回访 0否 1是' AFTER `business_type`;
