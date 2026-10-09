-- 删除积分商品兑换记录的优惠券快照字段。
-- 不兼容旧数据：执行后，历史兑换记录中的优惠券快照将永久丢失。
-- points_log 为逻辑表；如实际环境不存在该表，请跳过第一条，只执行对应的物理分表。
-- MySQL 5.7 不支持 DROP COLUMN IF EXISTS，执行前可先使用文末查询确认字段存在。
-- 发布顺序：先完成新版本应用的全量发布并确认旧实例全部下线，再执行删字段脚本。
-- 大表 DDL 可能等待元数据锁；建议维护窗口内逐表执行并观察锁等待、主从延迟和连接数。

ALTER TABLE `points_log` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_0` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_1` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_2` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_3` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_4` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_5` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_6` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_7` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_8` DROP COLUMN `coupon_snapshot`;
ALTER TABLE `points_log_9` DROP COLUMN `coupon_snapshot`;

-- 执行后应返回 0 行。
SELECT `TABLE_NAME`, `COLUMN_NAME`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `TABLE_NAME` IN ('points_log', 'points_log_0', 'points_log_1', 'points_log_2', 'points_log_3',
                       'points_log_4', 'points_log_5', 'points_log_6', 'points_log_7', 'points_log_8', 'points_log_9')
  AND `COLUMN_NAME` = 'coupon_snapshot'
ORDER BY `TABLE_NAME`;
