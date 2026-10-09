-- 一级会员用户券归档表初始化脚本。
--
-- 每张备份分表分两步创建：
-- 1. CREATE TABLE ... LIKE ...：复制对应 user_coupon_N 的全部字段、主键、索引和表参数，不复制数据。
-- 2. ALTER TABLE：追加 source_user_coupon_id，并增加唯一索引，保存和约束来源 user_coupon 主键。
--
-- 注意：
-- 1. user_coupon_bak_N 最终不是只有新增字段，而是“user_coupon_N 全部原字段 + source_user_coupon_id”。
-- 2. 归档 Mapper 使用 SELECT source.*, source.id，要求来源表与备份表的原字段结构和顺序保持一致。
-- 3. 本脚本为首次初始化脚本，执行前应确认 user_coupon_bak_0 ~ user_coupon_bak_9 尚未创建。

-- 分表 0：先复制 user_coupon_0 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_0` LIKE `user_coupon_0`;
ALTER TABLE `user_coupon_bak_0`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 1：先复制 user_coupon_1 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_1` LIKE `user_coupon_1`;
ALTER TABLE `user_coupon_bak_1`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 2：先复制 user_coupon_2 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_2` LIKE `user_coupon_2`;
ALTER TABLE `user_coupon_bak_2`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 3：先复制 user_coupon_3 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_3` LIKE `user_coupon_3`;
ALTER TABLE `user_coupon_bak_3`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 4：先复制 user_coupon_4 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_4` LIKE `user_coupon_4`;
ALTER TABLE `user_coupon_bak_4`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 5：先复制 user_coupon_5 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_5` LIKE `user_coupon_5`;
ALTER TABLE `user_coupon_bak_5`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 6：先复制 user_coupon_6 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_6` LIKE `user_coupon_6`;
ALTER TABLE `user_coupon_bak_6`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 7：先复制 user_coupon_7 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_7` LIKE `user_coupon_7`;
ALTER TABLE `user_coupon_bak_7`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 8：先复制 user_coupon_8 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_8` LIKE `user_coupon_8`;
ALTER TABLE `user_coupon_bak_8`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';

-- 分表 9：先复制 user_coupon_9 的完整结构，再追加来源用户券主键及唯一索引。
CREATE TABLE `user_coupon_bak_9` LIKE `user_coupon_9`;
ALTER TABLE `user_coupon_bak_9`
    ADD COLUMN `source_user_coupon_id` bigint unsigned NOT NULL COMMENT '来源用户优惠券ID',
    ADD UNIQUE KEY `uk_source_user_coupon_id` (`source_user_coupon_id`),
    COMMENT = '用户优惠券归档表';
