-- 在会员业务库执行。本脚本只检查索引并输出建议 SQL，不自动修改表或积分数据。
-- 部署补偿解冻任务前，确认 points_log 分表具备分页查询索引，避免历史积压时全表扫描。
SELECT t.table_name,
       CASE WHEN EXISTS (
           SELECT 1 FROM (
               SELECT table_schema, table_name, index_name,
                      GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS columns_in_order
               FROM information_schema.statistics
               WHERE table_schema = DATABASE()
               GROUP BY table_schema, table_name, index_name
           ) indexes_found
           WHERE indexes_found.table_schema = t.table_schema
             AND indexes_found.table_name = t.table_name
             AND (indexes_found.columns_in_order = 'business_id,sharding_value,points_type,deleted,create_time,points_log_id'
                  OR indexes_found.columns_in_order LIKE 'business_id,sharding_value,points_type,deleted,create_time,points_log_id,%')
       ) THEN '已存在可用索引'
       ELSE CONCAT('ALTER TABLE `', t.table_name,
                   '` ADD INDEX idx_points_unfreeze_scan (business_id,sharding_value,points_type,deleted,create_time,points_log_id);')
       END AS suggested_sql
FROM information_schema.tables t
WHERE t.table_schema = DATABASE()
  AND t.table_type = 'BASE TABLE'
  AND t.table_name REGEXP '^points_log(_[0-9])?$'
ORDER BY t.table_name;
