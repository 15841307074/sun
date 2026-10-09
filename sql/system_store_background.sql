CREATE TABLE `system_store_background_template` (
  `background_id` bigint(20) NOT NULL COMMENT '门店背景模板ID',
  `template_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `background_image` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店背景图片',
  `app_scope` tinyint(1) NOT NULL DEFAULT '1' COMMENT '应用范围：1按门店 2按标签',
  `store_scope` tinyint(1) DEFAULT NULL COMMENT '门店范围：1全部门店 2部分门店',
  `publish_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '发布状态：0关闭 1发布',
  `default_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '系统默认模板：0否 1是',
  `release_time` datetime DEFAULT NULL COMMENT '最后发布时间',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `business_id` bigint(20) DEFAUL  T NULL COMMENT '项目ID',
  PRIMARY KEY (`background_id`),
  KEY `idx_background_status_release` (`publish_status`,`default_flag`,`release_time`,`deleted`),
  KEY `idx_background_name` (`template_name`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店背景模板';

CREATE TABLE `system_store_background_store` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `background_id` bigint(20) NOT NULL COMMENT '门店背景模板ID',
  `store_id` bigint(20) NOT NULL COMMENT '门店ID',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `business_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`id`),
  KEY `idx_background_store_template` (`background_id`,`store_id`,`deleted`),
  KEY `idx_background_store_store` (`store_id`,`background_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店背景模板与门店关系';

CREATE TABLE `system_store_background_tag` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `background_id` bigint(20) NOT NULL COMMENT '门店背景模板ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `business_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`id`),
  KEY `idx_background_tag_template` (`background_id`,`tag_id`,`deleted`),
  KEY `idx_background_tag_tag` (`tag_id`,`background_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店背景模板与标签关系';

-- 标签模板按 tag_id 反查门店时使用，避免扫描 system_store_tag。
CREATE INDEX `idx_store_tag_tag_deleted`
  ON `system_store_tag` (`tag_id`, `deleted`, `store_id`);

-- 上线前将 background_image 替换为已上传的真实默认背景图地址，禁止置空。
INSERT INTO `system_store_background_template` (
  `background_id`, `template_name`, `background_image`, `app_scope`, `store_scope`,
  `publish_status`, `default_flag`, `release_time`, `creator`, `updater`, `business_id`
) VALUES (
  208900000000000000, '系统默认模板', 'https://pfiles.htyoudao.com/store/default-background.png',
  1, 1, 1, 1, NOW(), '', '', NULL
);
