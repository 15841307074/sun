-- 指定商品活动按 activity_id + commodity_id 联合过滤，联合索引避免读取活动下全部商品关系。
-- 上线前请先通过 SHOW INDEX 核对生产库是否已存在等价联合索引，避免重复创建。
ALTER TABLE activity_njnz_commodity
    ADD INDEX idx_njnz_activity_commodity (activity_id, commodity_id);

ALTER TABLE activity_mj_commodity
    ADD INDEX idx_mj_activity_commodity (activity_id, commodity_id);

ALTER TABLE activity_mz_commodity
    ADD INDEX idx_mz_activity_commodity (activity_id, commodity_id);
