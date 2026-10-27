-- 经营预警功能相关 SQL（MySQL 5.7）
-- 在业务 MySQL 库执行；不包含角色初始化、数据清理及其他业务脚本。
-- 仅创建尚不存在的功能表、补充缺失字段，不修改已有记录或已有字段类型。
-- store_anomaly_metrics_weekly 为原有周指标表，执行投诉字段部分前需已存在。
-- 不包含已取消的 analysis_store_org_snapshot。
SET NAMES utf8mb4;

-- 1. analysis_anomaly_user_report
CREATE TABLE IF NOT EXISTS `analysis_anomaly_user_report` (
  `id`
    BIGINT       NOT NULL                COMMENT '主键。不是数据库自增，是 MyBatis-Plus 的 ASSIGN_ID 雪花算法生成的，插入时由代码赋值',
  `business_id`
    BIGINT       NOT NULL                COMMENT '项目ID（业务线）。由 BusinessBaseDO 在插入时自动填充，不需要代码手动 set',
  `user_id`
    BIGINT       NOT NULL                COMMENT '接收人用户ID，对应 system_users.id。**颗粒度到人**，一个人一条',
  `anomaly_type`
    TINYINT      NOT NULL                COMMENT '异常类型，取值见 AnomalyTypeEnum：1=周度异常 2=月度货品销量异常 3=月度货品0订单异常。这一列同时决定了周期是按周还是按月，所以不需要再单独存一个 period_type。10 及以后预留给后续扩展（例如售卖异常）',
  `period_start`
    DATE         NOT NULL                COMMENT '周期开始日。周度存该周的周一（如 2026-09-14），月度存该月1号（如 2026-08-01）。**不存 period_end**，因为周=开始日+6天、月=该月最后一天，都能算出来，存了反而容易不一致',
  `store_count`
    INT          NOT NULL DEFAULT 0      COMMENT '统计门店：这个人管辖的门店总数。周度取表 analysis_user_store_snapshot 的快照数；月度来源待业务确认',
  `anomaly_metric_count`
    INT                   DEFAULT 0      COMMENT '异常指标：这个人的异常门店一共涉及几个异常指标（0~6）。**只有周度（anomaly_type=1）用这个字段**，月度记录里固定为 0',
  `anomaly_store_count`
    INT                   DEFAULT 0      COMMENT '异常门店：这个人管辖的门店里，有几家存在异常。**只有月度（anomaly_type=2/3）用这个字段**，周度记录里固定为 0',
  `creator`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '创建者。BaseDO 自动填充',
  `create_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间。BaseDO 自动填充',
  `updater`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '更新者。BaseDO 自动填充',
  `update_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间。BaseDO 自动填充',
  `deleted`
    BIT(1)       NOT NULL DEFAULT b'0'   COMMENT '是否逻辑删除。本表只增不删，这个字段是 BaseDO 的兼容字段，业务上不会用到',
  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_type_period_user`
    (`anomaly_type`, `period_start`, `user_id`, `business_id`)
    COMMENT '任务重跑幂等键。注意：ON DUPLICATE KEY UPDATE 时不会碰任何字段以外的内容',

  KEY `idx_user_period`
    (`user_id`, `deleted`, `period_start`)
    COMMENT 'App「经营异常消息查询」：某用户按周期倒序，也就是「经营预警」列表页的数据源'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='经营异常提醒-人维度报告';

-- 2. analysis_platform_message
CREATE TABLE IF NOT EXISTS `analysis_platform_message` (
  `id`
    BIGINT       NOT NULL                COMMENT '主键，MyBatis-Plus ASSIGN_ID 雪花算法生成',
  `business_id`
    BIGINT       NOT NULL                COMMENT '项目ID（业务线），BusinessBaseDO 自动填充',
  `user_id`
    BIGINT       NOT NULL                COMMENT '接收人用户ID，对应 system_users.id',
  `message_type`
    TINYINT      NOT NULL                COMMENT '消息类型，取值见 MessageTypeEnum：1=周度经营异常 2=月度货品销量异常 3=月度货品0订单异常。10 及以后预留给其他业务（例如售卖异常提醒）',
  `period_start`
    DATE         NOT NULL                COMMENT '业务周期开始日。三个用途：① 拼消息正文里的「第38周 / 2026年08月」 ② 点击消息跳转时定位到哪个周期 ③ 和 user_id/message_type 组成幂等键',
  `content`
    VARCHAR(512) NOT NULL                COMMENT '消息正文快照，例如「(2026年第38周)您管辖的门店存在经营异常，点击查看>」。这里是**存下来的快照**，不是每次查询现拼的，因为这是「推送记录」，要保证以后改了文案模板，历史消息内容不变',
  `title`
    VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '消息标题快照，例如「经营异常提醒」。以后新增消息类型可有自己的标题，历史消息展示不受文案配置变更影响',
  `read_status`
    BIT(1)       NOT NULL DEFAULT b'0'   COMMENT '是否已读：0=未读 1=已读。由 App 接口 PUT /analysis/app/platform-message/read 修改',
  `read_time`
    DATETIME              DEFAULT NULL   COMMENT '已读时间。未读时为 NULL；重复标记已读时**保持首次已读时间不变**',
  `push_status`
    TINYINT      NOT NULL DEFAULT 0      COMMENT '推送状态，取值见 PushStatusEnum：0=待推送 1=推送成功 2=推送失败 3=无需推送（该用户查不到设备号）。**这个字段是补偿机制的唯一入口**——没有它就没法区分「还没推」和「推失败了」',
  `push_error`
    VARCHAR(512)          DEFAULT NULL   COMMENT '推送失败原因，取云函数返回的 errCode + errMsg，超过512字符截断。排查「用户说没收到推送」时先看这个字段',
  `creator`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '创建者',
  `create_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间。App 消息列表按这个字段倒序，前端拿它算「3分钟前 / 1天前」',
  `updater`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '更新者',
  `update_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间。要注意：用户点「已读」也会刷新这个字段，所以不能用它当「推送时间」',
  `deleted`
    BIT(1)       NOT NULL DEFAULT b'0'   COMMENT '是否逻辑删除。BaseDO 兼容字段',
  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_user_type_period`
    (`user_id`, `message_type`, `period_start`, `business_id`)
    COMMENT '推送任务重跑幂等键',

  KEY `idx_user_time`
    (`user_id`, `deleted`, `create_time`)
    COMMENT 'App「平台消息查询」：某用户按创建时间倒序',

  KEY `idx_push`
    (`push_status`, `message_type`, `period_start`)
    COMMENT '推送任务按「状态 + 类型 + 周期」捞待推送记录'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='平台消息（推送记录）';

-- 3. analysis_user_store_snapshot
CREATE TABLE IF NOT EXISTS `analysis_user_store_snapshot` (
  `id`
    BIGINT       NOT NULL              COMMENT '主键，MyBatis-Plus ASSIGN_ID 雪花算法生成',
  `business_id`
    BIGINT       NOT NULL              COMMENT '项目ID（业务线），BusinessBaseDO 自动填充',
  `period_type`
    TINYINT      NOT NULL              COMMENT '周期类型：1=周 2=月。月初可能恰好是周一，只用 period_start 无法区分是周快照还是月快照，所以加了这个字段',
  `period_start`
    DATE         NOT NULL              COMMENT '周期开始日。周快照存该周周一，月快照存该月1号',
  `user_id`
    BIGINT       NOT NULL              COMMENT '用户ID，对应 system_users.id',
  `store_id`
    BIGINT       NOT NULL              COMMENT '门店ID，对应 system_store_info.store_id',
  `store_name`
    VARCHAR(255) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '门店名称（快照）。冗余存一份是为了避免每次展示都回查门店表。长度 255 和排序规则 utf8mb4_bin 都是对齐 system_store_info.store_name 的',
  `creator`
    VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '创建者',
  `create_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`
    VARCHAR(64)  NOT NULL DEFAULT ''   COMMENT '更新者',
  `update_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`
    BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否逻辑删除。重跑时是按周期**物理删除**再插入的（因为门店会被划走，必须能删掉行），这个字段是 BaseDO 兼容字段',
  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_type_period_user_store`
    (`period_type`, `period_start`, `user_id`, `store_id`, `business_id`)
    COMMENT '任务重跑幂等键',

  KEY `idx_user_period`
    (`user_id`, `period_type`, `period_start`)
    COMMENT '按人查当期门店明细 / 统计门店数'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='经营异常提醒-人×门店快照';

-- 4. analysis_anomaly_job_record
CREATE TABLE IF NOT EXISTS `analysis_anomaly_job_record` (
  `id`
    BIGINT       NOT NULL                COMMENT '主键。MyBatis-Plus ASSIGN_ID 雪花算法生成',
  `business_id`
    BIGINT       NOT NULL                COMMENT '项目ID（业务线），BusinessBaseDO 自动填充',
  `job_type`
    TINYINT      NOT NULL                COMMENT '任务类型，见 AnomalyJobTypeEnum：1=周快照 2=月快照 3=周聚合 4=月销量聚合 5=月0订单聚合 6=周推送 7=月销量推送 8=月0订单推送 9=上游周异常 10/11=已废弃 12=上游月货品订单异常表生成',
  `job_handler`
    VARCHAR(64)  NOT NULL                COMMENT 'XXL-Job handler名称，冗余存一份方便在数据库里直接看是哪个任务',
  `period_type`
    TINYINT      NOT NULL                COMMENT '周期类型：1=周 2=月',
  `period_start`
    DATE         NOT NULL                COMMENT '周期开始日，和报告表/快照表用的是同一个值',
  `job_status`
    TINYINT      NOT NULL                COMMENT '执行状态：0=失败 1=成功 2=运行中',
  `record_count`
    INT                   DEFAULT 0      COMMENT '处理记录数（快照条数/报告人数/推送条数），排查用',
  `error_msg`
    VARCHAR(512)          DEFAULT NULL   COMMENT '失败原因，异常消息截断512字符',
  `start_time`
    DATETIME     NOT NULL                COMMENT '任务开始时间',
  `end_time`
    DATETIME              DEFAULT NULL   COMMENT '任务结束时间，成功或失败时写',
  `creator`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '创建者，BaseDO 自动填充',
  `create_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，BaseDO 自动填充',
  `updater`
    VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '更新者，BaseDO 自动填充',
  `update_time`
    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，BaseDO 自动填充',
  `deleted`
    BIT(1)       NOT NULL DEFAULT b'0'   COMMENT '是否逻辑删除，BaseDO 兼容字段',
  PRIMARY KEY (`id`),

  UNIQUE KEY `uk_type_period`
    (`job_type`, `period_start`, `business_id`)
    COMMENT '重跑幂等键',

  KEY `idx_period`
    (`period_start`)
    COMMENT '按周期查所有任务状态'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='经营异常提醒-定时任务执行记录';

-- 5. 仅补充缺失字段；已有字段会跳过，不重复添加。

-- store_anomaly_metrics_weekly.complaint_order_ratio
SET @anomaly_field_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_anomaly_metrics_weekly' AND COLUMN_NAME = 'complaint_order_ratio') = 0,
  'ALTER TABLE `store_anomaly_metrics_weekly` ADD COLUMN `complaint_order_ratio` decimal(12,4) DEFAULT NULL COMMENT ''投诉订单占比，单位百分比；有效订单数为0时为NULL''',
  'SELECT 1'
);
PREPARE anomaly_field_stmt FROM @anomaly_field_ddl;
EXECUTE anomaly_field_stmt;
DEALLOCATE PREPARE anomaly_field_stmt;

-- store_anomaly_metrics_weekly.complaint_order_count
SET @anomaly_field_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_anomaly_metrics_weekly' AND COLUMN_NAME = 'complaint_order_count') = 0,
  'ALTER TABLE `store_anomaly_metrics_weekly` ADD COLUMN `complaint_order_count` bigint(20) unsigned DEFAULT ''0'' COMMENT ''投诉订单数，按投诉订单号去重统计''',
  'SELECT 1'
);
PREPARE anomaly_field_stmt FROM @anomaly_field_ddl;
EXECUTE anomaly_field_stmt;
DEALLOCATE PREPARE anomaly_field_stmt;

-- store_anomaly_metrics_weekly.valid_order_count
SET @anomaly_field_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_anomaly_metrics_weekly' AND COLUMN_NAME = 'valid_order_count') = 0,
  'ALTER TABLE `store_anomaly_metrics_weekly` ADD COLUMN `valid_order_count` bigint(20) DEFAULT ''0'' COMMENT ''门店有效订单数，统计周期内已完成状态的订单数量''',
  'SELECT 1'
);
PREPARE anomaly_field_stmt FROM @anomaly_field_ddl;
EXECUTE anomaly_field_stmt;
DEALLOCATE PREPARE anomaly_field_stmt;

-- store_anomaly_metrics_weekly.food_hygiene_complaint_order_count
SET @anomaly_field_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store_anomaly_metrics_weekly' AND COLUMN_NAME = 'food_hygiene_complaint_order_count') = 0,
  'ALTER TABLE `store_anomaly_metrics_weekly` ADD COLUMN `food_hygiene_complaint_order_count` bigint(20) unsigned DEFAULT ''0'' COMMENT ''食品卫生投诉订单数，business_type=3，按订单号去重统计''',
  'SELECT 1'
);
PREPARE anomaly_field_stmt FROM @anomaly_field_ddl;
EXECUTE anomaly_field_stmt;
DEALLOCATE PREPARE anomaly_field_stmt;

-- analysis_platform_message.title
SET @anomaly_field_ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'analysis_platform_message' AND COLUMN_NAME = 'title') = 0,
  'ALTER TABLE `analysis_platform_message` ADD COLUMN `title` varchar(128) NOT NULL DEFAULT '''' COMMENT ''消息标题快照，保存推送当时的消息标题''',
  'SELECT 1'
);
PREPARE anomaly_field_stmt FROM @anomaly_field_ddl;
EXECUTE anomaly_field_stmt;
DEALLOCATE PREPARE anomaly_field_stmt;
