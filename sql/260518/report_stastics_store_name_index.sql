ALTER TABLE `report_stastics`
    ADD INDEX `idx_deleted_store_name` (`deleted`, `store_name`);
