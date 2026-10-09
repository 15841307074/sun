-- 有奖问答任务记录表
-- 说明：
-- 1. 参考 lottery_task 保存任务完成、获得、消耗次数。
-- 2. 有奖问答按手机号统计任务，避免同一手机号不同 memberId 重复刷次数。
-- 3. period_key 按次数计算规则保存：按天为 yyyyMMdd，按场次为 yyyyMMdd_HHmmHHmm。

CREATE TABLE IF NOT EXISTS `activity_answer_task` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `member_id` bigint(20) DEFAULT NULL COMMENT '会员ID',
  `member_mobile` bigint(20) NOT NULL COMMENT '会员手机号快照',
  `task_type` int(1) NOT NULL COMMENT '任务类型 3下单 4分享 5浏览首页 6签到',
  `period_key` varchar(64) NOT NULL COMMENT '活动场次/次数周期标识',
  `task_date` date DEFAULT NULL COMMENT '任务日期',
  `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '完成次数',
  `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得答题次数',
  `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗答题次数',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_answer_task_period` (`activity_id`, `member_mobile`, `task_type`, `period_key`),
  KEY `idx_answer_task_member` (`activity_id`, `member_mobile`),
  KEY `idx_answer_task_date` (`activity_id`, `task_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='有奖问答任务记录表';
