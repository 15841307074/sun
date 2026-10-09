-- 有奖问答并发幂等索引，兼容逻辑表及 _0 至 _9 分表。
-- 执行前如已有重复数据，需要先清理重复记录，否则唯一索引无法创建。
DELIMITER $$

DROP PROCEDURE IF EXISTS `proc_answer_add_unique_index` $$
CREATE PROCEDURE `proc_answer_add_unique_index`()
BEGIN
    DECLARE i INT DEFAULT -1;
    DECLARE v_suffix VARCHAR(16);
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_index_exists INT DEFAULT 0;

    WHILE i < 10 DO
        SET v_suffix = IF(i = -1, '', CONCAT('_', i));

        SET v_table_name = CONCAT('activity_answer_record_detail', v_suffix);
        SELECT COUNT(1) INTO v_exists FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = v_table_name;
        SELECT COUNT(1) INTO v_index_exists FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = v_table_name AND index_name = 'uk_record_question';
        IF v_exists > 0 AND v_index_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name,
                '` ADD UNIQUE KEY `uk_record_question` (`record_id`, `question_id`)');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET v_table_name = CONCAT('activity_answer_task', v_suffix);
        SELECT COUNT(1) INTO v_exists FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = v_table_name;
        SELECT COUNT(1) INTO v_index_exists FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = v_table_name AND index_name = 'uk_answer_task_period';
        IF v_exists > 0 AND v_index_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name,
                '` ADD UNIQUE KEY `uk_answer_task_period` (`activity_id`, `member_mobile`, `task_type`, `period_key`)');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET v_table_name = CONCAT('activity_answer_reward_log', v_suffix);
        SELECT COUNT(1) INTO v_exists FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = v_table_name;
        SELECT COUNT(1) INTO v_index_exists FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = v_table_name AND index_name = 'uk_record_id';
        IF v_exists > 0 AND v_index_exists = 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name,
                '` ADD UNIQUE KEY `uk_record_id` (`record_id`)');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET i = i + 1;
    END WHILE;
END $$

CALL `proc_answer_add_unique_index`() $$
DROP PROCEDURE IF EXISTS `proc_answer_add_unique_index` $$

DELIMITER ;
