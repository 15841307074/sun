-- 修复已逻辑删除门店残留的有效标签关系（MySQL 5.7/8.0）。
-- 仅生成脚本，未执行。先部署deleteStore修复，再在目标数据库执行。
-- 不物理删除关系、不删除标签组/标签值、不处理仅停业但未删除的门店。
-- 以下项目、租户必须改成目标值；例如项目10、租户0。
SET @repair_business_id = 10;
SET @repair_tenant_id = 0;

-- 1. 预览待修复数量及样本。限定关系表项目、租户，按门店主键匹配同项目已删除门店。
SELECT COUNT(*) AS pending_relation_count
FROM system_store_tag r
JOIN system_store_info s ON s.store_id = r.store_id
    AND s.business_id = r.business_id
WHERE r.business_id = @repair_business_id
  AND r.tenant_id = @repair_tenant_id
  AND r.deleted = b'0' AND s.deleted = b'1';

SELECT r.id, r.store_id, s.store_name, r.tag_id, r.business_id, r.tenant_id
FROM system_store_tag r
JOIN system_store_info s ON s.store_id = r.store_id
    AND s.business_id = r.business_id
WHERE r.business_id = @repair_business_id
  AND r.tenant_id = @repair_tenant_id
  AND r.deleted = b'0' AND s.deleted = b'1'
ORDER BY r.id LIMIT 100;

-- 2. 确认样本后初始化游标，仅执行一次；不要在每批前重置。
-- 在独立连接、autocommit=1下执行；不要包在一个长事务里。
-- 建议先导出第1步对应的关系行备份。
SET @repair_cursor = 0;
SET @repair_ceiling = (
    SELECT COALESCE(MAX(id), 0) FROM system_store_tag
    WHERE business_id = @repair_business_id AND tenant_id = @repair_tenant_id
);

-- 3. 重复执行本节，每批扫描最多1000条当前项目的关系并提交一次UPDATE。
-- 游标按关系ID推进；affected_rows=0不代表完成，应以scan_finished=1为准。
SET @repair_batch_end = (
    SELECT MAX(batch.id) FROM (
        SELECT id FROM system_store_tag
        WHERE business_id = @repair_business_id AND tenant_id = @repair_tenant_id
          AND id > @repair_cursor AND id <= @repair_ceiling
        ORDER BY id LIMIT 1000
    ) batch
);

UPDATE system_store_tag r
JOIN system_store_info s ON s.store_id = r.store_id
    AND s.business_id = r.business_id
SET r.deleted = b'1', r.update_time = NOW()
WHERE r.business_id = @repair_business_id
  AND r.tenant_id = @repair_tenant_id
  AND r.id > @repair_cursor AND r.id <= @repair_batch_end
  AND r.deleted = b'0' AND s.deleted = b'1';

SELECT ROW_COUNT() AS affected_rows;
SET @repair_cursor = COALESCE(@repair_batch_end, @repair_ceiling);
SELECT @repair_cursor AS last_processed_id, @repair_ceiling AS scan_ceiling,
       (@repair_cursor >= @repair_ceiling) AS scan_finished;
-- 本节重复执行结束。

-- 4. 全部分批完成后复查，预期0；如果仍有数据，确认是否有并发旧版本删除请求。
SELECT COUNT(*) AS remaining_relation_count
FROM system_store_tag r
JOIN system_store_info s ON s.store_id = r.store_id
    AND s.business_id = r.business_id
WHERE r.business_id = @repair_business_id
  AND r.tenant_id = @repair_tenant_id
  AND r.deleted = b'0' AND s.deleted = b'1';

-- 注意：找不到门店实体的孤立关系、项目错配关系不自动修改，应单独核实。
-- 如果标签组仍被未删除门店引用，继续保留原有禁止删除规则。
-- SQL只修复数据库关系，若另有历史标签缓存残留，需按项目既有流程刷新。
