-- =============================================================================
-- 月度门店组织快照表
-- 用途：保存某项目在某个月份执行快照任务时，每家正常营业门店的组织归属和完整组织链。
-- 说明：本脚本只创建新表，不删除或修改任何已有表，可直接在生产库对应 schema 中执行。
-- =============================================================================

-- 创建月度门店组织快照表；IF NOT EXISTS 用于避免重复执行时报“表已存在”。
CREATE TABLE IF NOT EXISTS `analysis_store_org_snapshot` (
  -- MyBatis-Plus 雪花主键。
  `id` BIGINT NOT NULL COMMENT '主键，MyBatis-Plus ASSIGN_ID 雪花算法生成',

  -- 项目隔离字段；App 请求取登录项目，XXL-Job 取任务参数 businessId。
  `business_id` BIGINT NOT NULL COMMENT '项目ID',

  -- 月份统一保存为该自然月第一天，例如 2026-08 保存 2026-08-01。
  `snapshot_month` DATE NOT NULL COMMENT '快照月份，固定保存自然月第一天',

  -- 门店快照字段，保存当次任务执行时的门店信息。
  `store_id` BIGINT NOT NULL COMMENT '门店ID，对应 system_store_info.store_id',
  `store_name` VARCHAR(255) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '门店名称快照',
  `store_province` VARCHAR(64) DEFAULT NULL COMMENT '门店省份快照；为空时接口统一展示为未设置',

  -- 门店直属组织及其直接上级快照；没有所属组织的门店允许为空。
  `org_id` BIGINT DEFAULT NULL COMMENT '门店直属组织ID',
  `org_name` VARCHAR(255) DEFAULT NULL COMMENT '门店直属组织名称快照',
  `parent_org_id` BIGINT DEFAULT NULL COMMENT '直属组织的直接上级组织ID',
  `parent_org_name` VARCHAR(255) DEFAULT NULL COMMENT '直属组织的直接上级组织名称快照',

  -- 完整组织链从根组织排到直属组织；每个节点保存 id、name、parentId、level、sort。
  `org_path_json` JSON NOT NULL COMMENT '从根组织到直属组织的完整组织路径JSON',

  -- BaseDO / BusinessBaseDO 标准审计字段。
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否逻辑删除',

  -- 主键保证每条快照记录唯一定位。
  PRIMARY KEY (`id`),

  -- 同一项目同一月份每家门店只允许一条快照，保证重跑结果不重复。
  UNIQUE KEY `uk_business_month_store` (`business_id`, `snapshot_month`, `store_id`)
    COMMENT '同项目同月份门店唯一',

  -- 支持按当前项目和月份判断、读取整月快照。
  KEY `idx_business_month` (`business_id`, `snapshot_month`)
    COMMENT '按项目和月份查询快照',

  -- 支持按当前项目、月份和直属组织定位门店。
  KEY `idx_business_month_org` (`business_id`, `snapshot_month`, `org_id`)
    COMMENT '按项目月份和直属组织查询门店'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='经营异常提醒-月度门店组织快照';
