ALTER TABLE `good_coupon`
    CHANGE COLUMN `dept_ids` `community_qr_image` varchar(256) DEFAULT NULL COMMENT '社群二维码';

ALTER TABLE `coupon_package`
    ADD COLUMN `community_qr_image` varchar(256) DEFAULT NULL COMMENT '社群二维码' AFTER `underlined_content`;



ALTER TABLE activity ADD guide_image TEXT NULL COMMENT '引导图片';
ALTER TABLE lottery_settings ADD guide_image TEXT NULL COMMENT '引导图片';




-- auto-generated definition
create table activity_channel_name
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    name         varchar(255) default ''                not null comment '营销活动名称',
    is_enable    tinyint(1)   default 0                 not null comment '是否启用 1是 0否',
    channel_link varchar(512)                           null comment '渠道链接（备用）',
    creator      varchar(64)  default ''                not null comment '创建者',
    create_time  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater      varchar(64)  default ''                null comment '更新者',
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      bit          default b'0'              not null comment '是否删除 0否 1是',
    business_id  bigint                                 null comment '业务ID',
    is_default   tinyint(1)   default 0                 null comment '是否默认 1是 0否'
)
    comment '营销活动名称表' collate = utf8mb4_unicode_ci;




-- 插入默认渠道(微信H5路径)
INSERT INTO `activity_channel_name` (
    `name`,
    `is_enable`,
    `creator`,
    `is_default`,
    `deleted`,
    `business_id`  -- 新增business_id字段
) VALUES (
             '默认渠道(微信H5路径)',
             1,
             'admin',
             1,
             b'0',
             10  -- 赋值为10
         );

-- 插入默认渠道(社群活动路径)
INSERT INTO `activity_channel_name` (
    `name`,
    `is_enable`,
    `creator`,
    `is_default`,
    `deleted`,
    `business_id`  -- 新增business_id字段
) VALUES (
             '默认渠道(社群活动路径)',
             1,
             'admin',
             1,
             b'0',
             10  -- 赋值为10
         );

-- 插入默认渠道(小程序活动路径)
INSERT INTO `activity_channel_name` (
    `name`,
    `is_enable`,
    `creator`,
    `is_default`,
    `deleted`,
    `business_id`  -- 新增business_id字段
) VALUES (
             '默认渠道(小程序活动路径)',
             1,
             'admin',
             1,
             b'0',
             10  -- 赋值为10
         );







-- 执行前仍建议备份两张表数据
-- 步骤1：插入缺失的channel_name记录（统一排序规则）
INSERT INTO activity_channel_name (
    name,
    is_enable,
    creator,
    updater,
    deleted,
    is_default,
    channel_link,
    business_id
)
SELECT
    ac.channel_name,
    1,                     -- 默认开启
    'auto_script',         -- 创建者（可自定义）
    'auto_script',         -- 更新者（可自定义）
    0,                     -- 未删除
    0,                     -- 非默认
    '',
    NULL
FROM activity_channel ac
-- 关键修改：JOIN条件显式指定排序规则为utf8mb4_unicode_ci
         LEFT JOIN activity_channel_name acn
                   ON ac.channel_name COLLATE utf8mb4_unicode_ci = acn.name COLLATE utf8mb4_unicode_ci
WHERE
    acn.id IS NULL
  AND ac.channel_name IS NOT NULL
GROUP BY ac.channel_name;








-- 步骤2：更新channel_id（统一排序规则）
UPDATE activity_channel ac
    INNER JOIN activity_channel_name acn
ON ac.channel_name COLLATE utf8mb4_unicode_ci = acn.name COLLATE utf8mb4_unicode_ci
    SET
        ac.channel_id = acn.id,
        ac.business_id =  10,
        ac.updater = 'auto_script',
        ac.update_time = CURRENT_TIMESTAMP
WHERE ac.channel_id IS NULL OR ac.channel_id = 0;

-- 为activity_channel表新增is_enable字段
ALTER TABLE activity_channel
    ADD COLUMN is_enable tinyint(1)
    DEFAULT 1                 -- 默认值1（启用）
    NOT NULL                  -- 非空约束，确保字段必有值
    COMMENT '是否启用 1是 0否'  -- 字段注释
-- 可选：指定字段位置（如放在channel_id后，不指定则默认加在最后）
-- AFTER channel_id
;


ALTER TABLE `wx_member` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_0` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_1` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_2` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_3` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_4` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_5` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_6` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_7` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_8` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);
ALTER TABLE `wx_member_9` ADD INDEX `idx_bus_del_reg` (`business_id`, `deleted`, `register_time`);


备份activity_channel