ALTER TABLE `activity_cq`
    ADD COLUMN `start_date_time` datetime DEFAULT NULL COMMENT '参与开始时间' AFTER `browse_home_count`,
    ADD COLUMN `end_date_time` datetime DEFAULT NULL COMMENT '参与结束时间' AFTER `start_date_time`;

UPDATE `activity_cq` cq
    LEFT JOIN `activity` a ON a.`id` = cq.`activity_id`
SET cq.`start_date_time` = CASE
                               WHEN a.`start_date` IS NULL THEN NULL
                               ELSE CONCAT(DATE_FORMAT(a.`start_date`, '%Y-%m-%d'), ' 00:00:00')
    END,
    cq.`end_date_time` = CASE
                             WHEN a.`end_date` IS NULL THEN NULL
                             ELSE CONCAT(DATE_FORMAT(a.`end_date`, '%Y-%m-%d'), ' 23:59:59')
    END
WHERE cq.`start_date_time` IS NULL
   OR cq.`end_date_time` IS NULL;
