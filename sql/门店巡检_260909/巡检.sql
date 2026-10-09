ALTER TABLE `system_store_inspection_record`
    ADD COLUMN `rectification_opinion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '整改意见' AFTER `store_patrol_position`;