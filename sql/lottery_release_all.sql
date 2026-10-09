-- 抽奖改造统一发布 SQL · 2026-10-08
-- 覆盖：标签适用范围、V2 库存/请求/任务账本、门店标签通知、会员积分幂等、最近中奖、人工补发权限。
-- 仅本轮增量，不包含历史基础建表/旧活动数据迁移，不删除历史数据，不清零库存或次数。
--
-- 使用方法：在正确的实际数据库连接中设置下方执行类型，执行整个文件。
--   PROMOTION：营销主库，必须已有 activity、lottery_settings、lottery_prize。
--   SYSTEM   ：系统主库，必须已有 system_menu；包含标签通知表及人工补发按钮权限。
--   MEMBER   ：会员物理库，按该库已存在的 wx_member_0..9 建对应积分幂等分表。
--   LOG      ：中奖记录物理库，仅修改该库已存在的 lottery_log / lottery_log_0..9。
-- 库若分布在不同实例，分别用对应连接执行本文件；数据库同实例也不要把四类内容盲目建入同一库。
-- 示例：先选营销库，将类型设 PROMOTION 执行；再选系统库，将类型设 SYSTEM 执行。
-- 会员及日志有多个物理库时，对每个实际物理库执行对应类型。
-- 所有执行结果中 release_status 应为 READY；WRONG_TARGET_OR_DATABASE 表示本轮所有 DDL 均跳过。
-- 工具执行过程中若有 SQL 错误，应停止并核对，不使用忽略错误/--force。
--
-- 可重复执行：只新增不存在的字段/索引/表；已存在对象不会被覆盖，但仍需核对原有结构。
-- 包含 CHECK 约束，建议使用 MySQL 8.0.16+；本文件没有连接实际数据库验证。
-- 发布前暂停旧抽奖受理并处理在途任务；历史活动默认 runtime_version=1，禁止批量改为 2。
-- Nacos 仍需配置会员积分幂等分表路由，实际数据源必须与 wx_member / points_log 一致。
-- 人工补发不再扣次数或消耗积分；新增 JSON 快照字段不需要改表结构。
-- SYSTEM 类型只新增按钮，不给任何角色自动授权；执行后在角色管理按需授权。

SET NAMES utf8mb4;
SET @lottery_release_target = 'PROMOTION'; -- 只需改这一处：PROMOTION / SYSTEM / MEMBER / LOG
SET @lottery_release_target = UPPER(TRIM(@lottery_release_target));
SET @lottery_release_database = DATABASE();
SET @lottery_release_ready = CASE @lottery_release_target
  WHEN 'PROMOTION' THEN (SELECT COUNT(*)=3 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('activity','lottery_settings','lottery_prize'))
  WHEN 'SYSTEM' THEN EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_menu')
  WHEN 'MEMBER' THEN EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME REGEXP '^wx_member_[0-9]$')
  WHEN 'LOG' THEN EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME REGEXP '^lottery_log(_[0-9])?$')
  ELSE 0 END;
SET @lottery_do_promotion = @lottery_release_ready AND @lottery_release_target='PROMOTION';
SET @lottery_do_system = @lottery_release_ready AND @lottery_release_target='SYSTEM';
SET @lottery_do_member = @lottery_release_ready AND @lottery_release_target='MEMBER';
SET @lottery_do_log = @lottery_release_ready AND @lottery_release_target='LOG';
SELECT @lottery_release_database AS target_database,@lottery_release_target AS target_type,
       IF(@lottery_release_ready,'READY','WRONG_TARGET_OR_DATABASE') AS release_status;

-- ==================== A · PROMOTION：营销主库 ====================

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='activity' AND COLUMN_NAME='app_scope'),
  'ALTER TABLE `activity` ADD COLUMN `app_scope` TINYINT NOT NULL DEFAULT 0 COMMENT ''抽奖适用范围：0 按门店，1 按标签''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

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

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_prize' AND COLUMN_NAME='code'),
  'ALTER TABLE `lottery_prize` ADD COLUMN `code` VARCHAR(255) NULL COMMENT ''奖品稳定编码''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='config_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `config_version` BIGINT NOT NULL DEFAULT 1',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='stock_epoch'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `stock_epoch` BIGINT NOT NULL DEFAULT 1',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='runtime_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `runtime_version` INT NOT NULL DEFAULT 1 COMMENT ''1 旧流程；2 新版持久化抽奖，不自动回退''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='prize_template_json'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `prize_template_json` LONGTEXT NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='last_reset_scope'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `last_reset_scope` VARCHAR(128) NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_settings' AND COLUMN_NAME='cache_cleaned_version'),
  'ALTER TABLE `lottery_settings` ADD COLUMN `cache_cleaned_version` BIGINT NOT NULL DEFAULT 0',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

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

SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_counter (
  business_id BIGINT NOT NULL, activity_id BIGINT NOT NULL, member_id BIGINT NOT NULL,
  scope_key VARCHAR(128) COLLATE utf8mb4_bin NOT NULL, source INT NOT NULL,
  gained BIGINT NOT NULL DEFAULT 0, consumed BIGINT NOT NULL DEFAULT 0, finished BIGINT NOT NULL DEFAULT 0,
  revision BIGINT NOT NULL DEFAULT 1,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (business_id, activity_id, member_id, scope_key, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

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

SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_activity_counter (
  business_id BIGINT NOT NULL,activity_id BIGINT NOT NULL,total BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY(business_id,activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion,
  'CREATE TABLE IF NOT EXISTS lottery_v2_counter_cache_task (
  business_id BIGINT NOT NULL,activity_id BIGINT NOT NULL,member_id BIGINT NOT NULL,
  revision BIGINT NOT NULL,dirty TINYINT NOT NULL DEFAULT 1,next_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY(business_id,activity_id,member_id),KEY idx_lottery_quota_publish(dirty,next_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_promotion AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_job') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_v2_job' AND COLUMN_NAME='action'),
  'ALTER TABLE `lottery_v2_job` ADD COLUMN `action` VARCHAR(16) NOT NULL DEFAULT ''GRANT''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== B · SYSTEM：系统主库 ====================

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

SET @lottery_release_ddl = IF(@lottery_do_system AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_activity_tag_outbox') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_activity_tag_outbox' AND COLUMN_NAME='lease_token'),
  'ALTER TABLE `system_activity_tag_outbox` ADD COLUMN `lease_token` VARCHAR(64) NULL',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

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

SET @lottery_release_ddl = IF(@lottery_do_system,
  'SELECT id,parent_id,name,permission FROM system_menu WHERE permission=''promotion:lottery:reissue'' AND deleted=0',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== C · MEMBER：会员实际物理库 ====================
-- 每张积分幂等表必须与对应 wx_member_N 位于同一物理库；不会创建不属于当前库的多余分表。

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_0'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_0 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_1'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_1 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_2'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_2 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_3'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_3 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_4'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_4 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_5'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_5 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_6'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_6 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_7'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_7 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_8'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_8 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_member AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='wx_member_9'),
  'CREATE TABLE IF NOT EXISTS member_lottery_points_9 (business_id BIGINT NOT NULL,member_id BIGINT NOT NULL,sharding_value INT NOT NULL,business_no VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,delta INT NOT NULL,state VARCHAR(16) NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(business_id,member_id,business_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== D · LOG：中奖记录实际物理库 ====================
-- 历史 is_guarantees 保持 NULL，不自动回填，不将未知历史记录当作非兜底。

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_0` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_0` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_1` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_1` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_2` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_2` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_3` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_3` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_4` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_4` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_5` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_5` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_6` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_6` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_7` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_7` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_8` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_8` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9') AND NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9' AND COLUMN_NAME='is_guarantees'),
  'ALTER TABLE `lottery_log_9` ADD COLUMN `is_guarantees` TINYINT NULL COMMENT ''奖品保底快照：0 否，1 是，NULL 表示历史状态未知''',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

SET @lottery_release_ddl = IF(@lottery_do_log AND EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9') AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9' AND INDEX_NAME='idx_lottery_recent_winners'),
  'ALTER TABLE `lottery_log_9` ADD INDEX `idx_lottery_recent_winners` (`business_id`,`lottery_id`,`is_guarantees`,`create_time`,`id`)',
  'DO 0');
PREPARE lottery_release_stmt FROM @lottery_release_ddl;
EXECUTE lottery_release_stmt;
DEALLOCATE PREPARE lottery_release_stmt;

-- ==================== 执行后核对 ====================
SELECT @lottery_release_database AS target_database,@lottery_release_target AS target_type,
       IF(@lottery_release_ready,'COMPLETED_CHECK_OBJECTS','SKIPPED_WRONG_TARGET_OR_DATABASE') AS release_status;
SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE()
  AND (TABLE_NAME LIKE 'lottery_v2_%' OR TABLE_NAME LIKE 'member_lottery_points_%'
       OR TABLE_NAME IN ('activity_store_tag','system_activity_tag_outbox')) ORDER BY TABLE_NAME;
SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
  AND ((TABLE_NAME='activity' AND COLUMN_NAME='app_scope')
       OR (TABLE_NAME='lottery_settings' AND COLUMN_NAME IN ('config_version','stock_epoch','runtime_version','prize_template_json','last_reset_scope','cache_cleaned_version'))
       OR (TABLE_NAME REGEXP '^lottery_log(_[0-9])?$' AND COLUMN_NAME='is_guarantees'))
  ORDER BY TABLE_NAME,COLUMN_NAME;

-- 历史兜底快照回填不属于本次必执行 SQL：必须核对当时奖品配置后分批处理，未知数据保留 NULL。
-- 不会批量启用旧活动，不会把 runtime_version 全部改为 2，不会覆盖 V2 历史账本。
-- 未定义 USE 数据库名，必须先在 SQL 客户端选择实际数据库，再执行正确 target 类型。

