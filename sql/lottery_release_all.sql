-- 抽奖改造统一发布 SQL · 2026-10-12（生产环境增量）
-- 覆盖：标签适用范围、V2 库存/请求/任务账本、门店标签通知、会员积分幂等、最近中奖、人工补发权限。
-- 仅执行本轮新增对象与必要索引，不清零库存或次数，不迁移或删除旧表。
--
-- 使用方法：在 Navicat 查询窗口选择实际业务数据库，完整执行本文件一次；不需要填写模块类型或库名。
-- 本文件适用于活动、系统、会员积分和中奖记录都在同一个库的部署。
-- 当前库必须已有 activity、lottery_settings、lottery_prize；否则全部变更跳过并提示基础表缺失。
-- 脚本自动处理系统表、积分流水和中奖记录；仅修改已有的普通表或分片表，不创建缺失分片。
-- 已存在的字段和索引自动跳过；缺失的基础表会在执行前后的结果中显示，不能将跳过视为已完成。
-- 前置 release_status 应为 READY，最终为 COMPLETED_CHECK_OBJECTS。
-- 工具执行过程中若有 SQL 错误，应停止并核对，不使用忽略错误/--force。
--
-- 可重复执行：只新增不存在的字段/索引/表；已存在对象不会被覆盖，但仍需核对原有结构。
-- 包含 CHECK 约束，使用 MySQL 8.0.16+；请按执行结果核对实际业务库的表结构。
-- 执行前备份，并暂停旧版抽奖/发奖/缓存发布实例；完成本脚本后整体切换新版，历史活动禁止批量改为 V2。
-- 会员积分复用现有 wx_member / points_log 路由，无需新增 Nacos 分表规则；二者须在同一事务数据源。
-- 人工补发不再扣次数或消耗积分；新增 JSON 快照字段不需要改表结构。
-- 系统部分只新增按钮，不给任何角色自动授权；执行后在角色管理按需授权。

-- 说明：设置本连接字符集，确保中文注释及菜单名称正确写入。
-- 约定：每个变更块的 SET 根据当前库的基础表及对象是否已存在生成 DDL，否则生成 DO 0（跳过）。
-- 约定：随后 PREPARE 编译该语句，EXECUTE 执行，DEALLOCATE PREPARE 释放预处理句柄。
SET NAMES utf8mb4;
-- 说明：自动取得 Navicat 查询窗口当前选中的库名，不切换库、不要求手工填写库名。
SET @lottery_release_database = DATABASE();
-- 说明：检查已选择业务库且三个抽奖基础表齐全，避免误在空库或错误连接中创建业务表。
SET @lottery_release_ready = @lottery_release_database IS NOT NULL
  AND (SELECT COUNT(*)=3 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('activity','lottery_settings','lottery_prize'));
-- 说明：基础检查通过后执行本库所有活动和抽奖变更，不再按手动类型筛选。
SET @lottery_do_promotion = @lottery_release_ready;
-- 说明：本库存在系统菜单表时，同时执行标签通知和人工补发按钮变更。
SET @lottery_do_system = @lottery_release_ready AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_menu');
-- 说明：本库存在积分流水普通表或分片表时，同时执行对应的积分索引变更。
SET @lottery_do_member = @lottery_release_ready AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME REGEXP '^points_log(_[0-9])?$');
-- 说明：本库存在中奖记录普通表或分片表时，同时执行对应的中奖记录变更。
SET @lottery_do_log = @lottery_release_ready AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME REGEXP '^lottery_log(_[0-9])?$');
-- 说明：执行前展示实际库名及各类基础表是否存在；READY=可执行，缺失部分不执行。
SELECT @lottery_release_database AS target_database,
       IF(@lottery_release_ready,'READY','NO_DATABASE_OR_MISSING_LOTTERY_BASE_TABLES') AS release_status,
       @lottery_do_promotion AS lottery_enabled,@lottery_do_system AS system_enabled,
       @lottery_do_member AS points_enabled,@lottery_do_log AS log_enabled;

-- ==================== A · 活动和抽奖 ====================

-- 说明：为活动增加门店/标签适用范围标识，已有数据默认按门店。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='activity' AND COLUMN_NAME='app_scope'),
  'ALTER TABLE `activity` ADD COLUMN `app_scope` TINYINT NOT NULL DEFAULT 0 COMMENT ''抽奖适用范围：0 按门店，1 按标签''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建活动与门店标签关联表，供按标签确定适用门店。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS `activity_store_tag` (
    `id` BIGINT NOT NULL COMMENT ''主键'',
    `business_id` BIGINT NOT NULL COMMENT ''项目 ID'',
    `activity_id` BIGINT NOT NULL COMMENT ''活动 ID'',
    `tag_id` BIGINT NOT NULL COMMENT ''门店标签 ID'',
    `creator` VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''创建者'',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
    `updater` VARCHAR(64) DEFAULT '''' COMMENT ''更新者'',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
    `deleted` BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''是否删除'',
    PRIMARY KEY (`id`),
    KEY `idx_activity_store_tag_activity` (`business_id`, `activity_id`, `deleted`),
    KEY `idx_activity_store_tag_tag` (`business_id`, `tag_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''活动适用门店标签''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为奖品增加稳定编码，供库存和中奖记录关联。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_prize' AND COLUMN_NAME='code'),
  'ALTER TABLE `lottery_prize` ADD COLUMN `code` VARCHAR(255) NULL COMMENT ''奖品稳定编码''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：记录抽奖配置版本，区分配置变更前后的数据。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='config_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `config_version` BIGINT NOT NULL DEFAULT 1',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：记录库存批次，避免不同批次的库存混用。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='stock_epoch'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `stock_epoch` BIGINT NOT NULL DEFAULT 1',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：记录抽奖运行版本；旧活动默认保留旧流程。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='runtime_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `runtime_version` INT NOT NULL DEFAULT 1 COMMENT ''1 旧流程；2 新版持久化抽奖，不自动回退''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：保存奖品模板快照，供配置重建使用。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='prize_template_json'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `prize_template_json` LONGTEXT NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：记录上次重置范围，避免重复重置。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='last_reset_scope'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `last_reset_scope` VARCHAR(128) NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：记录已完成缓存清理的配置版本。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='cache_cleaned_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `cache_cleaned_version` BIGINT NOT NULL DEFAULT 0',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建 V2 库存账本；按活动、批次、奖池门店和奖品编码隔离，并约束预占加已发不超过总量。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_stock (
  business_id BIGINT NOT NULL, activity_id BIGINT NOT NULL, stock_epoch BIGINT NOT NULL,
  pool_store_id BIGINT NOT NULL COMMENT ''0 表示共用奖池'', prize_code VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  prize_id BIGINT NOT NULL, total BIGINT NOT NULL, reserved BIGINT NOT NULL DEFAULT 0,
  issued BIGINT NOT NULL DEFAULT 0, revision BIGINT NOT NULL DEFAULT 1,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (business_id, activity_id, stock_epoch, pool_store_id, prize_code),
  CONSTRAINT ck_lottery_stock CHECK (reserved >= 0 AND issued >= 0 AND total >= reserved + issued)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建 V2 抽奖请求账本；保存幂等请求、中奖结果、发奖状态和唯一红包单号。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_request (
  id BIGINT NOT NULL, business_id BIGINT NOT NULL, activity_id BIGINT NOT NULL, settings_id BIGINT NOT NULL,
  member_id BIGINT NOT NULL, store_id BIGINT NOT NULL, request_id VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  config_version BIGINT NOT NULL, stock_epoch BIGINT NOT NULL, pool_store_id BIGINT NOT NULL,
  prize_code VARCHAR(64) COLLATE utf8mb4_bin NOT NULL, prize_id BIGINT NOT NULL,
  draw_status VARCHAR(24) NOT NULL DEFAULT ''ACCEPTED'', grant_status VARCHAR(24) NOT NULL DEFAULT ''PENDING'',
  stock_state VARCHAR(16) NOT NULL DEFAULT ''RESERVED'', chance_source INT NOT NULL,
  scope_key VARCHAR(128) NOT NULL, points_cost INT NOT NULL DEFAULT 0,
  out_bill_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  snapshot_json LONGTEXT NOT NULL, result_json LONGTEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id), UNIQUE KEY uk_lottery_request (business_id, activity_id, member_id, request_id),
  UNIQUE KEY uk_lottery_bill (out_bill_no), KEY idx_lottery_pending (business_id, activity_id, draw_status),
  KEY idx_lottery_member_prize (business_id, activity_id, member_id, prize_code, stock_state)
  ,KEY idx_lottery_request_reference (business_id, member_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建 V2 抽奖次数账本；保存获得、消耗、完成次数及缓存发布标记。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_counter (
  business_id BIGINT NOT NULL, activity_id BIGINT NOT NULL, member_id BIGINT NOT NULL,
  scope_key VARCHAR(128) COLLATE utf8mb4_bin NOT NULL, source INT NOT NULL,
  gained BIGINT NOT NULL DEFAULT 0, consumed BIGINT NOT NULL DEFAULT 0, finished BIGINT NOT NULL DEFAULT 0,
  revision BIGINT NOT NULL DEFAULT 1,
  cache_dirty TINYINT NOT NULL DEFAULT 0,cache_next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (business_id, activity_id, member_id, scope_key, source),
  KEY idx_lottery_counter_publish (scope_key,source,cache_dirty,cache_next_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建 V2 异步发奖任务表；记录任务状态、重试和租约。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_job (
  request_pk BIGINT NOT NULL, state VARCHAR(16) NOT NULL DEFAULT ''READY'', attempts INT NOT NULL DEFAULT 0,
  action VARCHAR(16) NOT NULL DEFAULT ''GRANT'',
  next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), lease_token VARCHAR(64) NULL,
  lease_until DATETIME(3) NULL, last_error VARCHAR(512) NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (request_pk), KEY idx_lottery_job_poll (state, next_at, lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：创建 V2 配置缓存发布任务表；记录待发布版本和下次处理时间。
SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_cache_task (
  business_id BIGINT NOT NULL, activity_id BIGINT NOT NULL, settings_id BIGINT NOT NULL,
  config_version BIGINT NOT NULL, dirty TINYINT NOT NULL DEFAULT 1, attempts INT NOT NULL DEFAULT 0,
  next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (business_id, activity_id), KEY idx_lottery_cache_poll (dirty, next_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 汇总与次数缓存发布复用次数表；以下 ALTER 兼容已部署的 V2 表结构。
-- 说明：兼容已存在的 V2 次数表，补充缓存待发布标记。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter' AND COLUMN_NAME='cache_dirty'),
  'ALTER TABLE lottery_v2_counter ADD COLUMN cache_dirty TINYINT NOT NULL DEFAULT 0',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：兼容已存在的 V2 次数表，补充缓存下次发布时间。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter' AND COLUMN_NAME='cache_next_at'),
  'ALTER TABLE lottery_v2_counter ADD COLUMN cache_next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：兼容已存在的 V2 次数表，增加待发布记录查询索引。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_counter' AND INDEX_NAME='idx_lottery_counter_publish'),
  'ALTER TABLE lottery_v2_counter ADD INDEX idx_lottery_counter_publish (scope_key,source,cache_dirty,cache_next_at)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：兼容已存在的 V2 发奖任务表，补充任务动作字段。
SET @lottery_release_ddl = IF(@lottery_do_promotion AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_job') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_job' AND COLUMN_NAME='action'),
  'ALTER TABLE `lottery_v2_job` ADD COLUMN `action` VARCHAR(16) NOT NULL DEFAULT ''GRANT''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== B · 门店标签和菜单权限 ====================

-- 说明：创建门店标签变更通知表，供异步发送并失败重试。
SET @lottery_release_ddl = IF(@lottery_do_system,
  'CREATE TABLE IF NOT EXISTS system_activity_tag_outbox (
  id BIGINT NOT NULL,business_id BIGINT NOT NULL,store_id BIGINT NOT NULL,
  operation VARCHAR(16) NOT NULL,payload TEXT NOT NULL,done TINYINT NOT NULL DEFAULT 0,
  attempts INT NOT NULL DEFAULT 0,next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  lease_token VARCHAR(64) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY(id),KEY idx_store_tag_outbox(done,next_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：兼容已存在的标签通知表，补充任务租约令牌。
SET @lottery_release_ddl = IF(@lottery_do_system AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_activity_tag_outbox') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_activity_tag_outbox' AND COLUMN_NAME='lease_token'),
  'ALTER TABLE `system_activity_tag_outbox` ADD COLUMN `lease_token` VARCHAR(64) NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：若人工补发按钮不存在，则参考抽奖编辑菜单新增按钮权限；不会自动授权角色。
SET @lottery_release_ddl = IF(@lottery_do_system,
  'INSERT INTO system_menu
  (id,name,permission,type,sort,parent_id,path,icon,component,component_name,
   status,visible,keep_alive,always_show,ownership,creator,create_time,updater,update_time,deleted,business_show)
SELECT (UUID_SHORT() & 9223372036854775807),''人工补发'',''promotion:lottery:reissue'',3,99,
       base.parent_id,'''','''','''','''',0,base.visible,base.keep_alive,base.always_show,
       base.ownership,''migration'',CURRENT_TIMESTAMP,''migration'',CURRENT_TIMESTAMP,0,base.business_show
FROM system_menu base
WHERE base.permission=''promotion:lottery:update'' AND base.deleted=0
  AND NOT EXISTS (SELECT 1 FROM system_menu present
                  WHERE present.permission=''promotion:lottery:reissue'' AND present.deleted=0)
ORDER BY base.id LIMIT 1',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：查询人工补发按钮，供执行后核对权限是否创建。
SET @lottery_release_ddl = IF(@lottery_do_system,
  'SELECT id,parent_id,name,permission FROM system_menu WHERE permission=''promotion:lottery:reissue'' AND deleted=0',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== C · 会员积分流水索引 ====================
-- 复用 points_log，不新增积分账本表。仅为已有普通表或分片增加业务号查询索引，不变更会员余额。

-- 说明：若本库使用未分片的 points_log，也为它增加业务号查询索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_0 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_0') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_0' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_0` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_1 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_1') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_1' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_1` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_2 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_2') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_2' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_2` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_3 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_3') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_3' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_3` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_4 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_4') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_4' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_4` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_5 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_5') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_5' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_5` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_6 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_6') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_6' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_6` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_7 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_7') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_7' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_7` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_8 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_8') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_8' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_8` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为会员积分流水分片 points_log_9 增加按项目、会员和业务号查询的索引；表或索引不存在条件不满足时跳过。
SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_9') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='points_log_9' AND INDEX_NAME='idx_lottery_points_code'),
  'ALTER TABLE `points_log_9` ADD INDEX `idx_lottery_points_code` (`business_id`,`member_id`,`log_code`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== D · 中奖记录字段和索引 ====================
-- 历史 is_guarantees 保持 NULL，不自动回填，不将未知历史记录当作非兜底。

-- 说明：为中奖记录表 lottery_log 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_0 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_0` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_0 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_0` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_1 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_1` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_1 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_1` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_2 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_2` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_2 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_2` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_3 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_3` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_3 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_3` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_4 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_4` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_4 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_4` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_5 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_5` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_5 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_5` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_6 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_6` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_6 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_6` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_7 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_7` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_7 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_7` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_8 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_8` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_8 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_8` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_9 增加是否保底奖品的历史快照字段；旧记录保持 NULL。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_9` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：为中奖记录表 lottery_log_9 增加最近中奖查询索引。
SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_9` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- 说明：以下查询只读，用于人工确认本次执行目标及创建结果。
-- ==================== 执行后核对 ====================
-- 说明：再次确认目标库及前置表检查结果；此状态不能代替逐项对象核对。
SELECT @lottery_release_database AS target_database,
       CASE WHEN NOT @lottery_release_ready THEN 'SKIPPED_NO_DATABASE_OR_MISSING_LOTTERY_BASE_TABLES'
            WHEN @lottery_do_system AND @lottery_do_member AND @lottery_do_log THEN 'COMPLETED_CHECK_OBJECTS'
            ELSE 'COMPLETED_WITH_MISSING_BASE_TABLES' END AS release_status,
       @lottery_do_promotion AS lottery_enabled,@lottery_do_system AS system_enabled,
       @lottery_do_member AS points_enabled,@lottery_do_log AS log_enabled;
-- 说明：列出本库 V2 账本、活动标签和标签通知表，检查应建表是否存在。
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
  AND (TABLE_NAME LIKE 'lottery_v2_%'
       OR TABLE_NAME IN ('activity_store_tag','system_activity_tag_outbox')) ORDER BY TABLE_NAME;
-- 说明：列出活动范围、抽奖配置版本及中奖保底字段，检查应加字段是否存在。
SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
  AND ((TABLE_NAME='activity' AND COLUMN_NAME='app_scope')
       OR (TABLE_NAME='lottery_settings' AND COLUMN_NAME IN ('config_version','stock_epoch','runtime_version','prize_template_json','last_reset_scope','cache_cleaned_version'))
       OR (TABLE_NAME REGEXP '^lottery_log(_[0-9])?$' AND COLUMN_NAME='is_guarantees'))
  ORDER BY TABLE_NAME,COLUMN_NAME;

-- 说明：列出积分流水业务号索引，确认当前会员库各已有分片完成索引创建。
SELECT TABLE_NAME,INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE()
  AND INDEX_NAME='idx_lottery_points_code' ORDER BY TABLE_NAME,SEQ_IN_INDEX;

-- 历史兜底快照回填不属于本次必执行 SQL：必须核对当时奖品配置后分批处理，未知数据保留 NULL。
-- 不会批量启用旧活动，不会把 runtime_version 全部改为 2，不会覆盖 V2 历史账本。
-- 未定义 USE 数据库名；在 Navicat 查询窗口选择实际业务库后，执行整个文件即可，无需更改模块参数。

