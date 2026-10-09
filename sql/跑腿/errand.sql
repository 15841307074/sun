ALTER TABLE `wx_member`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_0`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_1`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_2`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_3`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_4`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_5`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_6`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_7`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_8`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

ALTER TABLE `wx_member_9`
    CHANGE COLUMN `project_owner_ship` `errand_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否是配送员 0不是 1是';

update wx_member SET errand_flag = 0;
update wx_member_1 SET errand_flag = 0;
update wx_member_2 SET errand_flag = 0;
update wx_member_3 SET errand_flag = 0;
update wx_member_4 SET errand_flag = 0;
update wx_member_5 SET errand_flag = 0;
update wx_member_6 SET errand_flag = 0;
update wx_member_7 SET errand_flag = 0;
update wx_member_8 SET errand_flag = 0;
update wx_member_9 SET errand_flag = 0;
update wx_member_0 SET errand_flag = 0;

ALTER TABLE `bz_errand_runner_balance_log`
    ADD COLUMN `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '可用余额' AFTER `after_balance`,
    ADD COLUMN `frozen_balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '冻结余额' AFTER `balance`;

ALTER TABLE `bz_errand_runner_balance_log`
    MODIFY COLUMN `flow_type` tinyint(4) NOT NULL COMMENT '流水类型：1赏金入账 2提现扣减 3退款扣回 4提现失败退回 5人工调整 6赏金解冻';
