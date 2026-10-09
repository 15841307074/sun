-- ============================================================
-- 积分商城：商品上下架、分类筛选及多媒体附件改造
-- 适用数据库：MySQL 8.x
--
-- 说明：
-- 1. 商品上下架继续使用 points_product.is_available：1 已上架，2 已下架。
-- 2. 商品售罄状态继续使用 points_product.product_status：1 已售罄，2 正常。
-- 3. 优惠券商品继续使用 points_product.coupon_code 关联优惠券，不新增 coupon_id。
-- 4. 本次数据库仅新增以下两个附件 JSON 字段。
-- 5. 历史 product_header_image、product_detail_images 字段继续保留，应用层会兼容旧数据。
-- 6. 商品独立封面图复用现有 product_thumbnail 字段，不重复新增数据库字段。
-- ============================================================

ALTER TABLE `points_product`
    ADD COLUMN `product_header_attachments` TEXT NULL
        COMMENT '商品头图附件JSON，最多5个，支持1视频和2图片'
        AFTER `product_header_image`,
    ADD COLUMN `product_detail_attachments` TEXT NULL
        COMMENT '商品详情附件JSON，最多5个，仅支持2图片'
        AFTER `product_detail_images`;

-- 附件 JSON 示例：
-- [
--   {"url":"https://example.com/product.jpg","type":2},
--   {"url":"https://example.com/product.mp4","type":1}
-- ]

-- 执行后校验字段是否创建成功。
SELECT `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_COMMENT`
FROM `information_schema`.`COLUMNS`
WHERE `TABLE_SCHEMA` = DATABASE()
  AND `TABLE_NAME` = 'points_product'
  AND `COLUMN_NAME` IN ('product_header_attachments', 'product_detail_attachments')
ORDER BY `ORDINAL_POSITION`;
