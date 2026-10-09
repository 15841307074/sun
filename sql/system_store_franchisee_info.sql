-- ============================================================
-- 门店管理：加盟商信息关系表
-- 适用数据库：MySQL 8.x
--
-- 说明：
-- 1. 每个门店最多一条加盟商信息，通过 store_id 唯一索引保证。
-- 2. 删除资料采用逻辑删除；再次保存时按 store_id 恢复原记录。
-- 3. 手机号、身份证号和银行卡号允许在不同门店重复，不建立唯一索引。
-- 4. 门店列表多标签筛选复用 system_store_tag，不需要新增字段或表。
-- ============================================================

CREATE TABLE IF NOT EXISTS `system_store_franchisee_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` bigint(20) NOT NULL COMMENT '门店ID',
  `franchisee_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加盟商姓名',
  `franchisee_mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加盟商手机号',
  `id_card_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加盟商身份证号',
  `bank_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
  `bank_province` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户省份',
  `bank_city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户城市',
  `bank_card_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行卡账号',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  `business_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_store_franchisee_info_store_id` (`store_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店加盟商信息关系表';

-- 执行后校验表结构及唯一索引。
SELECT `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_DEFAULT`, `COLUMN_COMMENT`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `TABLE_NAME` = 'system_store_franchisee_info'
ORDER BY `ORDINAL_POSITION`;

SHOW INDEX FROM `system_store_franchisee_info`;
