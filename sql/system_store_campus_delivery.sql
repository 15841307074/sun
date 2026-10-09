-- 门店校园配送字段与费用类型扩展
ALTER TABLE `system_store_info`
  ADD COLUMN `campus_delivery_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '校园配送开关 0支持 1不支持' AFTER `store_without_payment`,
  ADD COLUMN `campus_delivery_subsidy` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '校园配送补贴' AFTER `campus_delivery_status`;

ALTER TABLE `system_store_expenses`
  MODIFY COLUMN `store_expenses_type` tinyint(1) NULL DEFAULT 0 COMMENT '费用类型 0 堂食/外带打包费 1 外卖打包费 2 外卖配送费 3 校园配送费用';
