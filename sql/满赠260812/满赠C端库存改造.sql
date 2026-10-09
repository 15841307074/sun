-- 满赠活动扩展配置：一条记录对应 activity 主表中的一条满赠活动。
CREATE TABLE `activity_mz` (
                               `id` bigint(20) NOT NULL COMMENT '主键',
                               `activity_id` bigint(20) NOT NULL COMMENT '活动主表id',
                               `discount_type` int(11) DEFAULT NULL COMMENT '优惠类型（1满N元赠商品，2满N件赠商品）',
                               `discount_rules` int(11) DEFAULT NULL COMMENT '优惠规则（1阶梯优惠 2循环优惠）',
                               `place_order_type` int(11) DEFAULT NULL COMMENT '下单类型（1按品类限制，2按商品限制）',
                               `category_type` int(11) DEFAULT NULL COMMENT '参与品类（1全部，2仅单品，3仅套餐）',
                               `place_order_product` int(11) DEFAULT NULL COMMENT '下单商品（1全部商品，2指定商品）',
                               `gift_inventory_type` int(11) DEFAULT '1' COMMENT '门店奖励库存（1共用库存，2独立库存）',
                               `user_limit_type` int(11) DEFAULT '0' COMMENT '用户参与限制（0不限制，1每人每天，2每人最多）',
                               `user_limit_value` int(11) DEFAULT NULL COMMENT '用户参与限制次数（user_limit_type>0时生效）',
                               `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                               `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                               `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
                               PRIMARY KEY (`id`),
                               KEY `idx_activity_id` (`activity_id`),
                               KEY `idx_business_id` (`business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='满赠活动配置表';


-- 满赠活动指定参与商品：仅“按商品限制且指定商品”时写入。
CREATE TABLE `activity_mz_commodity` (
                                         `id` bigint(20) NOT NULL COMMENT '主键',
                                         `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                         `commodity_id` bigint(20) NOT NULL COMMENT '商品ID',
                                         `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
                                         `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                         `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                         `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_commodity_id` (`commodity_id`),
                                         KEY `idx_activity_id` (`activity_id`),
                                         KEY `idx_business_id` (`business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='满赠活动关联商品表';


-- 满赠赠品及库存汇总：共用库存时 store_id 为空，门店独立库存时每个门店一条配置。
CREATE TABLE `activity_mz_gift` (
                                    `id` bigint(20) NOT NULL COMMENT '主键',
                                    `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                    `threshold` decimal(10,2) NOT NULL COMMENT '优惠门槛（满赠金额/件数）',
                                    `gift_commodity_id` bigint(20) NOT NULL COMMENT '赠送商品ID',
                                    `store_id` bigint(20) DEFAULT NULL COMMENT '门店ID；共用库存时为空，门店独立库存时为对应门店ID',
                                    `gift_image` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '赠品图片',
                                    `gift_commodity_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '赠送商品名称',
                                    `gift_price` decimal(10,2) DEFAULT '0.00' COMMENT '赠送商品价格（连锁商品库售卖价）',
                                    `activity_inventory` int(11) DEFAULT NULL COMMENT '活动初始库存；NULL表示不限库存，0表示无库存，最大1000000',
                                    `remaining_inventory` int(11) DEFAULT '0' COMMENT '当前可用库存；订单提交锁定时扣减，取消或退款时恢复',
                                    `locked_inventory` int(11) NOT NULL DEFAULT '0' COMMENT '未支付订单锁定库存；提交锁定时增加，支付或取消时减少',
                                    `consumed_inventory` int(11) NOT NULL DEFAULT '0' COMMENT '已支付且未退款的净消耗库存；支付时增加，退款时减少',
                                    `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_gift_commodity_id` (`gift_commodity_id`),
                                    KEY `idx_business_id` (`business_id`),
                                    KEY `idx_mz_gift_activity_store_commodity` (`activity_id`,`store_id`,`gift_commodity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='满赠活动赠送商品表';


-- 满赠库存订单流水：用于锁定、支付确认、取消释放、退款恢复及超时补偿。
CREATE TABLE `activity_mz_gift_stock_record` (
                                                 `id` bigint(20) NOT NULL COMMENT '主键',
                                                 `activity_id` bigint(20) NOT NULL COMMENT '满赠活动ID',
                                                 `activity_mz_gift_id` bigint(20) DEFAULT NULL COMMENT 'activity_mz_gift赠品配置主键ID',
                                                 `store_id` bigint(20) DEFAULT NULL COMMENT '下单门店ID；门店独立库存用于定位库存Key，共用库存仅作订单归属记录',
                                                 `order_sn` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
                                                 `member_id` bigint(20) DEFAULT NULL COMMENT '用户ID，点餐机可为空',
                                                 `gift_commodity_id` bigint(20) NOT NULL COMMENT '赠品连锁商品ID',
                                                 `gift_sku_id` bigint(20) DEFAULT NULL COMMENT '赠品门店SKU ID',
                                                 `quantity` int(11) NOT NULL COMMENT '订单提交时实际成功锁定的赠品数量',
                                                 `inventory_type` tinyint(4) NOT NULL COMMENT '锁定时的库存模式快照：1所有门店共用库存，2门店独立库存',
                                                 `status` tinyint(4) NOT NULL COMMENT '流水状态：1锁定中，2已支付消耗，3取消/超时释放，4退款恢复',
                                                 `lock_expire_time` datetime DEFAULT NULL COMMENT '库存锁定截止时间；超过该时间仍锁定的流水进入补偿对账范围',
                                                 `paid_time` datetime DEFAULT NULL COMMENT '锁定库存转为已支付消耗的时间',
                                                 `released_time` datetime DEFAULT NULL COMMENT '未支付订单取消或超时释放库存的时间',
                                                 `refunded_time` datetime DEFAULT NULL COMMENT '已支付订单退款并恢复库存的时间',
                                                 `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                                 `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，即赠品库存锁定流水生成时间',
                                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除标识：0未删除，1已删除',
                                                 `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID，用于业务数据隔离',
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `uk_mz_stock_order_gift` (`order_sn`,`activity_id`,`gift_commodity_id`),
                                                 KEY `idx_mz_stock_expire` (`status`,`lock_expire_time`),
                                                 KEY `idx_mz_stock_activity_member` (`activity_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满赠赠品库存锁定流水';

-- 满赠用户参与记录：用于“每人每天”和“活动期间每人最多”参与次数校验。
-- 同一订单参与同一活动只记录一次，与该订单实际赠送件数无关；点餐机订单不写入。
CREATE TABLE `activity_mz_participation` (
                                             `id` bigint(20) NOT NULL COMMENT '主键',
                                             `activity_id` bigint(20) NOT NULL COMMENT '满赠活动ID',
                                             `member_id` bigint(20) NOT NULL COMMENT '参与活动的会员ID',
                                             `order_sn` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参与活动的订单号',
                                             `participation_date` date NOT NULL COMMENT '参与日期；每人每天限次时按该字段统计当天记录',
                                             `status` tinyint(4) NOT NULL COMMENT '参与状态：1锁定中，2已支付占用次数，3取消释放次数，4退款恢复次数',
                                             `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                             `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除标识：0未删除，1已删除',
                                             `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID，用于业务数据隔离',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `uk_mz_participation_order` (`activity_id`,`member_id`,`order_sn`),
                                             KEY `idx_mz_participation_limit` (`activity_id`,`member_id`,`status`,`participation_date`),
                                             KEY `idx_mz_participation_order_status` (`order_sn`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='满赠用户参与记录';
