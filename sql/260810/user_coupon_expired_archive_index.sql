-- 历史过期用户券归档索引。
--
-- 归档查询固定使用：
-- WHERE expiration_time <= ?
-- ORDER BY expiration_time, id
-- LIMIT ?
-- 因此使用 (expiration_time, id) 可以直接按索引范围顺序取一个小批次，
-- 避免每批扫描整表或对全部历史记录执行 filesort。
--
-- 上线前先执行 SHOW INDEX FROM user_coupon_N，已有同列顺序索引的表不要重复创建。
-- 大表创建索引应安排在低峰期，并确认数据库版本支持在线 DDL。

ALTER TABLE `user_coupon_0` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_1` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_2` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_3` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_4` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_5` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_6` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_7` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_8` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
ALTER TABLE `user_coupon_9` ADD INDEX `idx_expired_archive` (`expiration_time`, `id`);
