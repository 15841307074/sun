-- 骑手入驻：学校名称改为门店 ID
-- 说明：
-- 1. 旧字段 school_name 保存学校名称，本次改为 store_id 保存门店 ID。
-- 2. 脚本兼容重复执行：如果字段已改为 store_id，则只校准字段类型和注释。

SET @schema_name = DATABASE();

SELECT COUNT(1)
INTO @school_name_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND COLUMN_NAME = 'school_name';

SELECT COUNT(1)
INTO @store_id_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND COLUMN_NAME = 'store_id';

SET @rename_sql = IF(
    @school_name_exists > 0 AND @store_id_exists = 0,
    'ALTER TABLE `bz_errand_runner` CHANGE COLUMN `school_name` `store_id` bigint NULL DEFAULT NULL COMMENT ''门店ID''',
    'SELECT ''bz_errand_runner.store_id column rename skipped'' AS message'
);

PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(1)
INTO @store_id_exists_after
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND COLUMN_NAME = 'store_id';

SET @modify_sql = IF(
    @store_id_exists_after > 0,
    'ALTER TABLE `bz_errand_runner` MODIFY COLUMN `store_id` bigint NULL DEFAULT NULL COMMENT ''门店ID''',
    'SELECT ''bz_errand_runner.store_id column modify skipped'' AS message'
);

PREPARE stmt FROM @modify_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(1)
INTO @store_id_index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'bz_errand_runner'
  AND INDEX_NAME = 'idx_store_id';

SET @index_sql = IF(
    @store_id_exists_after > 0 AND @store_id_index_exists = 0,
    'ALTER TABLE `bz_errand_runner` ADD INDEX `idx_store_id` (`store_id`)',
    'SELECT ''bz_errand_runner.idx_store_id index skipped'' AS message'
);

PREPARE stmt FROM @index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
