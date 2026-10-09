-- 小程序按城市查询门店列表使用精确匹配，组合索引用于缓存冷加载和全量预热。
-- 上线前请先确认生产库不存在同名索引。
ALTER TABLE `system_store_info`
    ADD INDEX `idx_store_city_list_cache`
        (`business_id`, `store_city`, `deleted`, `store_source`, `store_status`, `order_type`);
