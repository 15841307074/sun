CREATE TABLE `wx_member_card_benefit_ref` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                              `member_card_id` bigint DEFAULT NULL COMMENT '会员卡ID',
                                              `benefit_scene` tinyint DEFAULT NULL COMMENT '权益场景',
                                              `coupon_type` tinyint DEFAULT NULL COMMENT '优惠券类型',
                                              `coupon_id` bigint DEFAULT NULL COMMENT '优惠券ID',
                                              `send_num` int DEFAULT NULL COMMENT '发放数量',
                                              `repeat_type` tinyint DEFAULT NULL COMMENT '重复类型',
                                              `issue_value` decimal(10,2) DEFAULT NULL COMMENT '发行价值',
                                              `business_id` varchar(64) DEFAULT NULL COMMENT '业务ID',
                                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                              `creator` varchar(32) DEFAULT NULL COMMENT '创建人',
                                              `updater` varchar(32) DEFAULT NULL COMMENT '更新人',
                                              `deleted` tinyint DEFAULT '0' COMMENT '删除标识 0未删除 1已删除',
                                              PRIMARY KEY (`id`),

    -- 🔥 核心索引：完全匹配你的查询 SQL（最关键！）
                                              KEY `idx_member_card_deleted` (`member_card_id`,`deleted`),

    -- 🔥 排序优化索引：配合 ORDER BY
                                              KEY `idx_benefit_scene_id` (`benefit_scene`,`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员卡权益关联表';






SELECT
    m.member_id,
    m.member_mobile,
    m.integral_frozen,
    m.member_level AS old_member_level,
    c.member_card_id,
    c.name AS member_card_name,
    c.min_points_threshold,
    c.max_points_threshold,
    c.member_level AS new_member_level
FROM wx_member_0 m
         JOIN wx_member_card c
              ON m.integral_frozen >= c.min_points_threshold
                  AND m.integral_frozen <= c.max_points_threshold
                  AND c.deleted = b'0'
                  AND c.card_status = 1
                  AND m.business_id = c.business_id
WHERE m.deleted = b'0'
  AND m.member_status = 0
  AND (
    m.member_level IS NULL
        OR m.member_level <> c.member_level
    );


UPDATE wx_member_0 m
    JOIN wx_member_card c
ON m.integral_frozen >= c.min_points_threshold
    AND m.integral_frozen <= c.max_points_threshold
    AND c.deleted = b'0'
    AND c.card_status = 1
    AND m.business_id = c.business_id
    SET m.member_level = c.member_level,
        m.update_time = NOW()
WHERE m.deleted = b'0'
  AND m.member_status = 0
  AND (
    m.member_level IS NULL
   OR m.member_level <> c.member_level
    );
