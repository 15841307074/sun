-- 学习任务热点查询索引。代码可独立上线；本脚本未自动执行。
-- 在目标环境先核对 EXPLAIN 与现有索引；同前缀索引已存在时跳过。

-- 文件进度按项目、学习人、资料关系和当前修订读取，避免扫描该资料全部历史修订。
SET @learning_index_exists = (
    SELECT COUNT(*) FROM (
        SELECT index_name,
               GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS columns_in_order
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'bpm_learning_task_file_progress'
        GROUP BY index_name
    ) existing_indexes
    WHERE index_name = 'idx_ltfp_current_revision'
       OR columns_in_order LIKE 'business_id,learner_user_id,task_material_id,progress_revision,deleted%'
);
SET @learning_index_sql = IF(@learning_index_exists > 0,
    'SELECT ''idx_ltfp_current_revision or equivalent already exists'' AS result',
    'ALTER TABLE bpm_learning_task_file_progress ADD INDEX idx_ltfp_current_revision (business_id,learner_user_id,task_material_id,progress_revision,deleted), ALGORITHM=INPLACE, LOCK=NONE');
PREPARE learning_index_statement FROM @learning_index_sql;
EXECUTE learning_index_statement;
DEALLOCATE PREPARE learning_index_statement;

-- 负责人门店是详情权限和订货校验的共同入口，先缩小到当前项目当前用户的正常门店。
SET @learning_index_exists = (
    SELECT COUNT(*) FROM (
        SELECT index_name,
               GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',') AS columns_in_order
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'system_store_info'
        GROUP BY index_name
    ) existing_indexes
    WHERE index_name = 'idx_store_learning_manager'
       OR columns_in_order LIKE 'business_id,user_id,store_status,deleted%'
);
SET @learning_index_sql = IF(@learning_index_exists > 0,
    'SELECT ''idx_store_learning_manager or equivalent already exists'' AS result',
    'ALTER TABLE system_store_info ADD INDEX idx_store_learning_manager (business_id,user_id,store_status,deleted), ALGORITHM=INPLACE, LOCK=NONE');
PREPARE learning_index_statement FROM @learning_index_sql;
EXECUTE learning_index_statement;
DEALLOCATE PREPARE learning_index_statement;
