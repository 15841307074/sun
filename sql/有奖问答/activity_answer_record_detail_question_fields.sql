-- 有奖问答答题明细题目字段补充脚本
-- 说明：开始答题时会把当时题目逐条写入明细表，后续继续答题、题目分析、导出都以明细表为准。
-- 覆盖逻辑表和 0-9 分表；如果某个表不存在会自动跳过。

DROP PROCEDURE IF EXISTS add_activity_answer_record_detail_question_fields;

DELIMITER $$

CREATE PROCEDURE add_activity_answer_record_detail_question_fields()
BEGIN
    DECLARE i INT DEFAULT -1;
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_table_exists INT DEFAULT 0;
    DECLARE v_column_exists INT DEFAULT 0;
    DECLARE v_index_exists INT DEFAULT 0;

    WHILE i < 10 DO
        IF i = -1 THEN
            SET v_table_name = 'activity_answer_record_detail';
        ELSE
            SET v_table_name = CONCAT('activity_answer_record_detail_', i);
        END IF;

        SELECT COUNT(1)
          INTO v_table_exists
          FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = v_table_name;

        IF v_table_exists > 0 THEN
            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'question_type';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `question_type` tinyint(1) DEFAULT 1 COMMENT ''题目类型 1选择题'' AFTER `question_id`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'question_title';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `question_title` varchar(2000) DEFAULT NULL COMMENT ''题目名称'' AFTER `question_type`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'question_tips';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `question_tips` varchar(1000) DEFAULT NULL COMMENT ''题目提示文案'' AFTER `question_title`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'options_json';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `options_json` text COMMENT ''选项JSON'' AFTER `question_tips`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'correct_answer';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `correct_answer` varchar(20) DEFAULT NULL COMMENT ''正确答案选项编码'' AFTER `options_json`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND COLUMN_NAME = 'is_answered';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD COLUMN `is_answered` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否已作答 0未作答 1已作答'' AFTER `correct_answer`');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` MODIFY COLUMN `answer_result` int(1) DEFAULT NULL COMMENT ''作答结果 0错误 1正确''');
            PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

            SET @sql = CONCAT(
                'UPDATE `', v_table_name, '` d ',
                'LEFT JOIN `activity_answer_question` q ON q.id = d.question_id AND q.deleted = b''0'' ',
                'SET d.is_answered = CASE WHEN d.selected_answer IS NOT NULL OR d.answer_result IS NOT NULL THEN 1 ELSE d.is_answered END, ',
                'd.question_type = COALESCE(d.question_type, q.question_type, 1), ',
                'd.question_title = COALESCE(d.question_title, q.question_title), ',
                'd.question_tips = COALESCE(d.question_tips, q.question_tips), ',
                'd.options_json = COALESCE(d.options_json, q.options_json), ',
                'd.correct_answer = COALESCE(d.correct_answer, q.correct_answer), ',
                'd.answer_time = COALESCE(d.answer_time, q.answer_time) ',
                'WHERE d.deleted = b''0'''
            );
            PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

            SELECT COUNT(1) INTO v_index_exists
              FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND INDEX_NAME = 'idx_record_question';
            IF v_index_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD INDEX `idx_record_question` (`record_id`, `question_id`)');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_index_exists
              FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = v_table_name
               AND INDEX_NAME = 'idx_activity_answered';
            IF v_index_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` ADD INDEX `idx_activity_answered` (`activity_id`, `is_answered`)');
                PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
            END IF;
        END IF;

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

CALL add_activity_answer_record_detail_question_fields();

DROP PROCEDURE IF EXISTS add_activity_answer_record_detail_question_fields;
