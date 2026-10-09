-- 学习任务建表脚本（兼容 MySQL 5.7+，不使用 WITH RECURSIVE）
-- 执行建议：先在预发逐条执行并核对 EXPLAIN；生产使用 DDL 工具限速执行。
-- 设计要点：资料实时关联 material_id；主进度按用户唯一；多门店只保存轻量投影；访问PV/UV写ES。

CREATE TABLE IF NOT EXISTS `bpm_learning_task` (
  `task_id` bigint NOT NULL COMMENT '学习任务ID',
  `task_name` varchar(50) NOT NULL COMMENT '任务名称',
  `cover_image_url` varchar(500) NOT NULL COMMENT '封面地址',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `task_description` varchar(500) DEFAULT NULL,
  `apply_scope` tinyint NOT NULL COMMENT '1按门店 2按标签',
  `store_scope` tinyint DEFAULT NULL COMMENT '1全部 2部分',
  `order_limit_type` tinyint NOT NULL DEFAULT 0,
  `order_limit_days` smallint DEFAULT NULL,
  `online_status` tinyint NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_lt_business_page` (`business_id`,`deleted`,`create_time`,`task_id`),
  KEY `idx_lt_online_period` (`business_id`,`deleted`,`online_status`,`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_attachment` (
  `attachment_id` bigint NOT NULL, `task_id` bigint NOT NULL,
  `attachment_url` varchar(500) NOT NULL, `attachment_type` tinyint NOT NULL, `sort` int NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`attachment_id`), KEY `idx_lta_task_sort` (`task_id`,`deleted`,`sort`,`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务附件';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_store` (
  `id` bigint NOT NULL, `task_id` bigint NOT NULL, `store_id` bigint NOT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_lts_task_store` (`task_id`,`store_id`),
  KEY `idx_lts_store_task` (`store_id`,`deleted`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务指定门店';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_tag` (
  `id` bigint NOT NULL, `task_id` bigint NOT NULL, `tag_id` bigint NOT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_ltt_task_tag` (`task_id`,`tag_id`),
  KEY `idx_ltt_tag_task` (`tag_id`,`deleted`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务门店标签';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_material` (
  `task_material_id` bigint NOT NULL, `task_id` bigint NOT NULL, `material_id` bigint NOT NULL,
  `document_browse_limit_type` tinyint NOT NULL DEFAULT 0, `min_document_browse_seconds` int DEFAULT NULL,
  `video_speed_allowed` bit(1) NOT NULL DEFAULT b'0', `required_flag` bit(1) NOT NULL DEFAULT b'1', `sort` int NOT NULL DEFAULT 0,
  `attachment_fingerprint` char(64) NOT NULL COMMENT '当前附件集合SHA-256',
  `progress_revision` int NOT NULL DEFAULT 0 COMMENT '附件变化时递增',
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`task_material_id`), UNIQUE KEY `uk_ltm_task_material` (`task_id`,`material_id`),
  KEY `idx_ltm_task_sort` (`task_id`,`deleted`,`sort`,`task_material_id`),
  KEY `idx_ltm_material` (`material_id`,`deleted`,`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务资料关系';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_learner_progress` (
  `progress_id` bigint NOT NULL, `task_id` bigint NOT NULL, `learner_user_id` bigint NOT NULL,
  `study_status` tinyint NOT NULL DEFAULT 0, `progress_rate` decimal(5,2) NOT NULL DEFAULT 0,
  `required_material_count` int NOT NULL DEFAULT 0, `completed_material_count` int NOT NULL DEFAULT 0,
  `valid_study_seconds` bigint NOT NULL DEFAULT 0, `start_time` datetime DEFAULT NULL,
  `last_study_time` datetime DEFAULT NULL, `completed_time` datetime DEFAULT NULL, `version` int NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`progress_id`), UNIQUE KEY `uk_ltlp_task_user` (`task_id`,`learner_user_id`),
  KEY `idx_ltlp_task_status` (`task_id`,`deleted`,`study_status`,`learner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务用户主进度';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_material_progress` (
  `progress_id` bigint NOT NULL, `task_id` bigint NOT NULL, `task_material_id` bigint NOT NULL,
  `material_id` bigint NOT NULL, `learner_user_id` bigint NOT NULL, `progress_revision` int NOT NULL,
  `study_status` tinyint NOT NULL DEFAULT 0, `progress_rate` decimal(5,2) NOT NULL DEFAULT 0,
  `valid_study_seconds` bigint NOT NULL DEFAULT 0, `start_time` datetime DEFAULT NULL,
  `last_study_time` datetime DEFAULT NULL, `completed_time` datetime DEFAULT NULL, `version` int NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`progress_id`),
  UNIQUE KEY `uk_ltmp_relation_user_rev` (`task_material_id`,`learner_user_id`,`progress_revision`),
  KEY `idx_ltmp_task_user_rev` (`task_id`,`learner_user_id`,`progress_revision`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务资料主进度';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_file_progress` (
  `file_progress_id` bigint NOT NULL, `task_id` bigint NOT NULL, `task_material_id` bigint NOT NULL,
  `material_id` bigint NOT NULL, `learner_user_id` bigint NOT NULL,
  `file_key` char(64) NOT NULL COMMENT '文件稳定键SHA-256', `file_type` tinyint NOT NULL,
  `file_version` char(64) NOT NULL COMMENT '文件版本SHA-256', `progress_revision` int NOT NULL,
  `valid_study_seconds` bigint NOT NULL DEFAULT 0, `last_position_seconds` bigint NOT NULL DEFAULT 0,
  `completed_flag` bit(1) NOT NULL DEFAULT b'0', `reached_bottom` bit(1) NOT NULL DEFAULT b'0',
  `start_time` datetime DEFAULT NULL, `last_study_time` datetime DEFAULT NULL, `completed_time` datetime DEFAULT NULL,
  `version` int NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`file_progress_id`),
  UNIQUE KEY `uk_ltfp_relation_user_file_rev` (`task_material_id`,`learner_user_id`,`file_key`,`progress_revision`),
  KEY `idx_ltfp_task_user_rev` (`task_id`,`learner_user_id`,`progress_revision`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务文件进度';

CREATE TABLE IF NOT EXISTS `bpm_learning_task_store_progress` (
  `projection_id` bigint NOT NULL, `task_id` bigint NOT NULL, `store_id` bigint NOT NULL, `learner_user_id` bigint NOT NULL,
  `study_status` tinyint NOT NULL DEFAULT 0, `progress_rate` decimal(5,2) NOT NULL DEFAULT 0,
  `valid_study_seconds` bigint NOT NULL DEFAULT 0, `start_time` datetime DEFAULT NULL,
  `last_study_time` datetime DEFAULT NULL, `completed_time` datetime DEFAULT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', `business_id` bigint NOT NULL,
  PRIMARY KEY (`projection_id`), UNIQUE KEY `uk_ltsp_task_store` (`task_id`,`store_id`),
  KEY `idx_ltsp_task_status` (`task_id`,`deleted`,`study_status`,`store_id`),
  KEY `idx_ltsp_task_user` (`task_id`,`learner_user_id`,`deleted`,`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务门店进度投影';

-- ES 索引由应用按月懒创建：bpm_learning_task_access_yyyy-MM。
-- 文档ID：businessId:taskId:visitId；visitId由前端生成并重试，409视为幂等成功。
