-- 在会员库首次执行一次。用独立连接建表，避免 DDL 提交其他事务。
CREATE TABLE IF NOT EXISTS `points_available_reconcile_audit` (
    `repair_no` char(36) NOT NULL COMMENT '本次修复编号',
    `business_id` bigint NOT NULL COMMENT '项目ID',
    `member_id` bigint NOT NULL COMMENT '会员ID',
    `shard_no` tinyint NOT NULL COMMENT '分表编号',
    `repair_action` varchar(20) NOT NULL COMMENT '补流水或补会员余额',
    `available_before` int NOT NULL COMMENT '修复前会员可用积分',
    `ledger_before` decimal(65,0) NOT NULL COMMENT '修复前积分流水合计，不含冻结',
    `target_points` int NOT NULL COMMENT '两边预计修复后的积分',
    `available_after` int DEFAULT NULL COMMENT '修复后实际会员可用积分',
    `ledger_after` decimal(65,0) DEFAULT NULL COMMENT '修复后实际积分流水合计，不含冻结',
    `inserted_points_log_id` bigint DEFAULT NULL COMMENT '本次补写的积分流水主键',
    `check_result` varchar(24) NOT NULL COMMENT '修复校验结果',
    `created_at` datetime NOT NULL COMMENT '修改前留档时间',
    `checked_at` datetime DEFAULT NULL COMMENT '修复后校验时间',
    PRIMARY KEY (`repair_no`),
    KEY `idx_member_created_at` (`business_id`, `member_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员可用积分对账修复留档';
