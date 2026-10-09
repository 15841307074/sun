-- 有奖问答红包返参字段，兼容逻辑表及 _0 至 _9 分表。
DELIMITER $$

DROP PROCEDURE IF EXISTS `proc_answer_reward_log_add_red_packet_fields` $$
CREATE PROCEDURE `proc_answer_reward_log_add_red_packet_fields`()
BEGIN
    DECLARE i INT DEFAULT -1;
    DECLARE v_table_name VARCHAR(128);
    DECLARE v_table_exists INT DEFAULT 0;
    DECLARE v_column_exists INT DEFAULT 0;

    WHILE i < 10 DO
        SET v_table_name = IF(i = -1, 'activity_answer_reward_log', CONCAT('activity_answer_reward_log_', i));
        SELECT COUNT(1) INTO v_table_exists
        FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = v_table_name;

        IF v_table_exists > 0 THEN
            SELECT COUNT(1) INTO v_column_exists
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = v_table_name AND column_name = 'package_info';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name,
                    '` ADD COLUMN `package_info` varchar(2000) DEFAULT NULL COMMENT ''红包调起支付参数'' AFTER `claim_status`');
                PREPARE stmt FROM @sql;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;
            END IF;

            SELECT COUNT(1) INTO v_column_exists
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = v_table_name AND column_name = 'out_bill_no';
            IF v_column_exists = 0 THEN
                SET @sql = CONCAT('ALTER TABLE `', v_table_name,
                    '` ADD COLUMN `out_bill_no` varchar(64) DEFAULT NULL COMMENT ''红包商户转账单号'' AFTER `package_info`');
                PREPARE stmt FROM @sql;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;
            END IF;
        END IF;

        SET i = i + 1;
    END WHILE;
END $$

CALL `proc_answer_reward_log_add_red_packet_fields`() $$
DROP PROCEDURE IF EXISTS `proc_answer_reward_log_add_red_packet_fields` $$

DELIMITER ;
