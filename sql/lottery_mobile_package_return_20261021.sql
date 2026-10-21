-- 抽奖 2026-10-21 增量：在现有 lottery_v2_* 所在业务库执行。
-- 不删除旧账本、不清空中奖记录；上线时应无进行中的抽奖活动。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `lottery_member_identity` (
  `business_id` bigint NOT NULL COMMENT '项目编号，隔离不同项目',
  `activity_id` bigint NOT NULL COMMENT '活动主表编号，非抽奖配置编号',
  `member_mobile` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '会员绑定手机号，映射参与身份',
  `member_id` bigint NOT NULL COMMENT '实际参与账号会员编号，用于发奖和定位分片',
  `member_category` int DEFAULT NULL COMMENT '会员渠道：1支付宝；其他沿用现有会员渠道定义；历史未知为空',
  `creator` varchar(64) NOT NULL DEFAULT 'lottery' COMMENT '创建人',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT 'lottery' COMMENT '更新人',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '删除标记：0正常，1已删除',
  PRIMARY KEY (`business_id`,`activity_id`,`member_mobile`,`member_id`),
  KEY `idx_lottery_identity_mobile` (`business_id`,`member_mobile`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖手机号与各渠道会员身份映射，不是次数计数表';

CREATE TABLE IF NOT EXISTS `lottery_mobile_counter` (
  `business_id` bigint NOT NULL COMMENT '项目编号',
  `activity_id` bigint NOT NULL COMMENT '活动主表编号',
  `member_mobile` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '次数和任务的唯一参与人标识，不包含会员ID',
  `scope_key` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '周期：TOTAL、日期、场次；ORDER:订单号为订单奖励防重回执',
  `source` int NOT NULL COMMENT '次数来源：0上限统计，1免费，2积分，3下单，4分享，5浏览',
  `gained` bigint NOT NULL DEFAULT 0 COMMENT '该周期该来源累计获得的机会数',
  `consumed` bigint NOT NULL DEFAULT 0 COMMENT '该周期该来源累计消耗的机会数',
  `finished` bigint NOT NULL DEFAULT 0 COMMENT '该周期该任务累计完成次数',
  `revision` bigint NOT NULL DEFAULT 1 COMMENT '缓存版本号，只允许新版本覆盖旧版本',
  `cache_dirty` tinyint NOT NULL DEFAULT 0 COMMENT '次数缓存待确认标记：1待确认，0已确认',
  `cache_next_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '允许人工恢复缓存的时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`business_id`,`activity_id`,`member_mobile`,`scope_key`,`source`),
  KEY `idx_lottery_mobile_publish` (`scope_key`,`source`,`cache_dirty`,`cache_next_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖手机号共享次数及任务账本，旧会员账本保留不迁移';

CREATE TABLE IF NOT EXISTS `lottery_coupon_package_grant` (
  `business_id` bigint NOT NULL COMMENT '项目编号',
  `request_pk` bigint NOT NULL COMMENT '原抽奖请求主键，券包整体发放的唯一业务编号',
  `plan_json` mediumtext NOT NULL COMMENT '首次发券计划：券包关系、固定优惠券ID及有效期；人工补发不得重新生成',
  `state` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '整体发放状态：PENDING待完成，SUCCESS全部完成；失败原因在原抽奖任务中',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '计划创建时间，即首次实际发券尝试时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`business_id`,`request_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖券包固定发放计划，防止部分成功及人工补发时重复发券';

CREATE TABLE IF NOT EXISTS `lottery_legacy_return` (
  `business_id` bigint NOT NULL COMMENT '项目编号',
  `log_id` bigint NOT NULL COMMENT '旧版中奖记录编号，一条中奖记录最多退库存一次',
  `primary_stock_key` varchar(255) NOT NULL COMMENT '原奖品实际使用的主库存缓存键',
  `legacy_stock_key` varchar(255) NOT NULL COMMENT '旧配置编号兼容库存缓存键，可与主键相同',
  `prize_cache_key` varchar(255) NOT NULL COMMENT '退回后需要失效的奖品列表缓存键',
  `lock_key` varchar(255) NOT NULL COMMENT '与旧版抽奖一致的奖品锁键',
  `state` varchar(16) NOT NULL DEFAULT 'READY' COMMENT '缓存同步状态：READY待处理，DONE已完成',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '退回回执创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`business_id`,`log_id`),
  KEY `idx_lottery_return_pending` (`state`,`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版抽奖实物回收回执，支持库存缓存失败后幂等恢复';

-- 以下索引段在中奖记录表所在库执行；同库部署可以整份执行。
-- 仅给实际存在且未建立该索引的表增加索引，不会新增不存在的分片表。
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_0' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_0` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_1' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_1` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_2' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_2` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_3' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_3` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_4' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_4` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_5' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_5` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_6' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_6` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_7' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_7` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_8' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_8` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
SET @lottery_identity_index_sql = IF(
  EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9')
  AND NOT EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='lottery_log_9' AND INDEX_NAME='idx_lottery_phone_records'),
  'ALTER TABLE `lottery_log_9` ADD INDEX `idx_lottery_phone_records` (`business_id`,`member_mobile`,`lottery_id`,`create_time`,`id`)', 'DO 0');
PREPARE lottery_identity_stmt FROM @lottery_identity_index_sql;
EXECUTE lottery_identity_stmt;
DEALLOCATE PREPARE lottery_identity_stmt;
