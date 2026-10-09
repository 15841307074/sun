-- 使用旧字段 product_detail_images 的第一张图片回填商品缩略图。
-- 兼容范围：仅处理 product_thumbnail 为 NULL 或空字符串的记录，不覆盖已有缩略图。
-- product_detail_images 由旧代码使用英文逗号分隔，本脚本取第一个非空片段。

-- 1. 执行前预览待回填数据。
SELECT `product_id`,
       `product_detail_images`,
       TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1)) AS `new_product_thumbnail`
FROM `points_product`
WHERE (`product_thumbnail` IS NULL OR TRIM(`product_thumbnail`) = '')
  AND `product_detail_images` IS NOT NULL
  AND TRIM(`product_detail_images`) <> ''
  AND TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1)) <> '';
t
-- 2. 回填缩略图。product_thumbnail 长度为 255，超过长度的数据暂不更新，避免被截断。
UPDATE `points_product`
SET `product_thumbnail` = TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1))
WHERE (`product_thumbnail` IS NULL OR TRIM(`product_thumbnail`) = '')
  AND `product_detail_images` IS NOT NULL
  AND TRIM(`product_detail_images`) <> ''
  AND TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1)) <> ''
  AND CHAR_LENGTH(TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1))) <= 255;

-- 3. 检查因首张图片 URL 超过 255 字符而未回填的数据。
SELECT `product_id`,
       CHAR_LENGTH(TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1))) AS `thumbnail_length`,
       TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1)) AS `product_thumbnail`
FROM `points_product`
WHERE (`product_thumbnail` IS NULL OR TRIM(`product_thumbnail`) = '')
  AND `product_detail_images` IS NOT NULL
  AND TRIM(`product_detail_images`) <> ''
  AND CHAR_LENGTH(TRIM(SUBSTRING_INDEX(`product_detail_images`, ',', 1))) > 255;

