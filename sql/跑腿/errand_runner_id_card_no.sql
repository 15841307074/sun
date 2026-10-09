-- 骑手入驻：新增身份证号字段
-- 说明：
-- 1. id_card_no 保存骑手入驻填写的身份证号。
-- 2. 代码层按 deleted=false 校验身份证号不能被其他会员占用。
-- 3. 添加普通索引用于加速唯一校验查询，避免软删除历史数据导致唯一索引冲突。

SET @schema_name = DATABASE();

SELECT COUNT(1)
INTO @id_card_no_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND COLUMN_NAME = 'id_card_no';

SET @add_column_sql = IF(
    @id_card_no_exists = 0,
    'ALTER TABLE `bz_errand_runner` ADD COLUMN `id_card_no` varchar(32) NULL DEFAULT NULL COMMENT ''身份证号'' AFTER `student_no`',
    'SELECT ''bz_errand_runner.id_card_no column skipped'' AS message'
);

PREPARE stmt FROM @add_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(1)
INTO @id_card_no_index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND INDEX_NAME = 'idx_id_card_no';

SET @add_index_sql = IF(
    @id_card_no_index_exists = 0,
    'ALTER TABLE `bz_errand_runner` ADD INDEX `idx_id_card_no` (`id_card_no`)',
    'SELECT ''bz_errand_runner.idx_id_card_no index skipped'' AS message'
);

PREPARE stmt FROM @add_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
