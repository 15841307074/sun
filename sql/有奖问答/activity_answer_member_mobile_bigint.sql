-- 有奖问答 member_mobile 字段改为数字类型
-- 说明：手机号为 11 位，MySQL int 会溢出，因此这里使用 bigint(20)。
-- 覆盖逻辑表和分表：activity_answer_record、activity_answer_record_detail、activity_answer_reward_log、activity_answer_task。

DELIMITER $$

DROP PROCEDURE IF EXISTS `proc_activity_answer_member_mobile_bigint` $$
CREATE PROCEDURE `proc_activity_answer_member_mobile_bigint`()
BEGIN
    DECLARE i INT DEFAULT -1;
    DECLARE v_table_name VARCHAR(128);
    DECLARE suffix VARCHAR(16);
    DECLARE table_exists INT DEFAULT 0;

    WHILE i < 10 DO
        IF i = -1 THEN
            SET suffix = '';
        ELSE
            SET suffix = CONCAT('_', i);
        END IF;

        SET v_table_name = CONCAT('activity_answer_record', suffix);
        SELECT COUNT(1) INTO table_exists
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = v_table_name;
        IF table_exists > 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` MODIFY COLUMN `member_mobile` bigint(20) NOT NULL COMMENT ''会员手机号快照''');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET v_table_name = CONCAT('activity_answer_record_detail', suffix);
        SELECT COUNT(1) INTO table_exists
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = v_table_name;
        IF table_exists > 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` MODIFY COLUMN `member_mobile` bigint(20) NOT NULL COMMENT ''会员手机号快照''');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET v_table_name = CONCAT('activity_answer_reward_log', suffix);
        SELECT COUNT(1) INTO table_exists
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = v_table_name;
        IF table_exists > 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` MODIFY COLUMN `member_mobile` bigint(20) NOT NULL COMMENT ''会员手机号快照''');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET v_table_name = CONCAT('activity_answer_task', suffix);
        SELECT COUNT(1) INTO table_exists
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = v_table_name;
        IF table_exists > 0 THEN
            SET @sql = CONCAT('ALTER TABLE `', v_table_name, '` MODIFY COLUMN `member_mobile` bigint(20) NOT NULL COMMENT ''会员手机号快照''');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;

        SET i = i + 1;
    END WHILE;
END $$

CALL `proc_activity_answer_member_mobile_bigint`() $$
DROP PROCEDURE IF EXISTS `proc_activity_answer_member_mobile_bigint` $$

DELIMITER ;
