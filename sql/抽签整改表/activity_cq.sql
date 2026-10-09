/*
 Navicat Premium Data Transfer

 Source Server         : 0090-local-业务库
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : 192.168.31.141:3307
 Source Schema         : cloud_0090_test

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 15/04/2026 13:02:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_cq
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq`;
CREATE TABLE `activity_cq` (
                               `id` bigint(20) NOT NULL COMMENT '主键id',
                               `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                               `activity_cover_image` varchar(255) DEFAULT NULL COMMENT '活动封面图',
                               `activity_background_image` varchar(255) DEFAULT NULL COMMENT '活动背景图',
                               `activity_details_image` varchar(255) DEFAULT NULL COMMENT '活动详情长图',
                               `button_background_color` varchar(255) DEFAULT NULL COMMENT '背景/按钮颜色',
                               `share_title` varchar(255) DEFAULT NULL COMMENT '分享标题',
                               `share_note` varchar(255) DEFAULT NULL COMMENT '分享内容',
                               `share_img_url` varchar(500) DEFAULT NULL COMMENT '分享图片',
                               `daily_attendance` int(10) NOT NULL DEFAULT '0' COMMENT '每日签到（0关闭 1开启）',
                               `daily_atten_count` int(10) DEFAULT NULL COMMENT '签到获得抽签码次数',
                               `free_event` int(1) NOT NULL DEFAULT '0' COMMENT '免费集签开关（0关闭 1开启）',
                               `free_count` int(10) DEFAULT NULL COMMENT '免费抽签次数',
                               `place_order_status` int(1) NOT NULL DEFAULT '0' COMMENT '下单获得（0关闭 1开启）',
                               `place_order_type` int(1) DEFAULT NULL COMMENT '下单类型（1 按品类限制 2 按商品限制）',
                               `category_type` int(1) DEFAULT NULL COMMENT '参与品类（1 全部 2 仅单品 3 仅套餐）',
                               `place_order_product` int(1) DEFAULT NULL COMMENT '下单商品（1 全部商品 2 指定商品）',
                               `payment_threshold` int(1) DEFAULT NULL COMMENT '支付门槛（0 不限制 1 限制）',
                               `payment_count` decimal(11,2) DEFAULT NULL COMMENT '支付金额门槛',
                               `place_order_code` int(1) DEFAULT '0' COMMENT '下单抽签码次数类型（0 每人每天 1 不限制）',
                               `place_order_code_number` int(11) DEFAULT NULL COMMENT '下单可获得抽签码次数',
                               `share_event` int(1) NOT NULL DEFAULT '0' COMMENT '分享活动（0关闭 1开启）',
                               `share_count` int(11) DEFAULT NULL COMMENT '分享可获得抽签码次数',
                               `browse_home_event` int(1) NOT NULL DEFAULT '0' COMMENT '浏览首页集签开关（0关闭 1开启）',
                               `browse_home_count` int(11) DEFAULT NULL COMMENT '浏览首页每人每天上限获得次数',
                               `start_date_time` datetime DEFAULT NULL COMMENT '参与开始时间',
                               `end_date_time` datetime DEFAULT NULL COMMENT '参与结束时间',
                               `result_publish_time` datetime DEFAULT NULL COMMENT '结果公布时间',
                               `draw_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '开奖状态 0未开奖 1开奖中 2已开奖',
                               `public_button` int(1) DEFAULT '0' COMMENT '公开展示（0开启 1关闭）',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                               `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                               `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                               `business_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
                               `share_type` int(1) NOT NULL DEFAULT '2' COMMENT '1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友',
                               PRIMARY KEY (`id`),
                               KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动设置表';

-- ----------------------------
-- Table structure for activity_cq_join
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join`;
CREATE TABLE `activity_cq_join` (
                                    `id` bigint(20) NOT NULL COMMENT '主键id',
                                    `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                    `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                    `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                    `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                    `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                    `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                    KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_0
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_0`;
CREATE TABLE `activity_cq_join_0` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_1
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_1`;
CREATE TABLE `activity_cq_join_1` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_2
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_2`;
CREATE TABLE `activity_cq_join_2` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_3
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_3`;
CREATE TABLE `activity_cq_join_3` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_4
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_4`;
CREATE TABLE `activity_cq_join_4` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_5
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_5`;
CREATE TABLE `activity_cq_join_5` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_6
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_6`;
CREATE TABLE `activity_cq_join_6` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_7
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_7`;
CREATE TABLE `activity_cq_join_7` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_8
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_8`;
CREATE TABLE `activity_cq_join_8` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_join_9
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_join_9`;
CREATE TABLE `activity_cq_join_9` (
                                      `id` bigint(20) NOT NULL COMMENT '主键id',
                                      `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                      `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                      `first_join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
                                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                      `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                      `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                      `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_activity_member` (`activity_id`,`member_id`),
                                      KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动参与记录表';

-- ----------------------------
-- Table structure for activity_cq_log
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log`;
CREATE TABLE `activity_cq_log` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                   `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                   `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                   `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                   `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                   `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0、保密；1、男；2、女',
                                   `member_category` tinyint(4) DEFAULT NULL COMMENT '用户类别 0 微信 1支付宝',
                                   `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                   `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                   `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                   `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                   `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                   `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                   `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                   `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                   `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                   `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                   `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                   `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                   `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                   `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                   `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                   `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                   `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                   `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                   KEY `idx_result_status` (`result_status`),
                                   KEY `idx_prize_type` (`prize_type`),
                                   KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                   KEY `idx_activity_result_category_member` (`activity_id`,`result_status`,`member_category`,`member_id`),
                                   KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_0
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_0`;
CREATE TABLE `activity_cq_log_0` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_1
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_1`;
CREATE TABLE `activity_cq_log_1` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_2
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_2`;
CREATE TABLE `activity_cq_log_2` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_3
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_3`;
CREATE TABLE `activity_cq_log_3` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_4
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_4`;
CREATE TABLE `activity_cq_log_4` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_5
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_5`;
CREATE TABLE `activity_cq_log_5` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_6
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_6`;
CREATE TABLE `activity_cq_log_6` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_7
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_7`;
CREATE TABLE `activity_cq_log_7` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_8
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_8`;
CREATE TABLE `activity_cq_log_8` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

-- ----------------------------
-- Table structure for activity_cq_log_9
-- ----------------------------
DROP TABLE IF EXISTS `activity_cq_log_9`;
CREATE TABLE `activity_cq_log_9` (
                                     `id` bigint(20) NOT NULL COMMENT '主键id',
                                     `activity_id` bigint(20) NOT NULL COMMENT '活动id',
                                     `sign_code` varchar(64) NOT NULL COMMENT '签码',
                                     `member_id` bigint(20) NOT NULL COMMENT '会员id',
                                     `member_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
                                     `member_mobile` varchar(50) DEFAULT NULL COMMENT '联系方式',
                                     `obtain_type` int(1) NOT NULL COMMENT '获取方式（1 每日签到 2 下单获得 3 分享活动 4 其他）',
                                     `store_id` bigint(20) DEFAULT NULL COMMENT '抽签门店id',
                                     `store_name` varchar(255) DEFAULT NULL COMMENT '抽签门店',
                                     `draw_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抽签时间',
                                     `result_status` int(1) NOT NULL DEFAULT '0' COMMENT '结果状态（0 待开奖 1 未中签 2 已中签）',
                                     `prize_id` bigint(20) DEFAULT NULL COMMENT '奖品id',
                                     `prize_type` int(1) DEFAULT NULL COMMENT '奖品类型（0 未中奖 1 优惠券 2 积分 3 实物 4 无奖品 5 现金红包 6 优惠券包）',
                                     `prize_content` varchar(500) DEFAULT NULL COMMENT '奖品内容',
                                     `prize_img_url` varchar(500) DEFAULT NULL COMMENT '奖品图片',
                                     `prize_state` int(1) DEFAULT NULL COMMENT '奖品状态（0 已发放 1 未填写收货地址 2 已填写地址待发货 3 已发货 9 已退回）',
                                     `receive_user` varchar(255) DEFAULT NULL COMMENT '收件人',
                                     `receive_mobile` varchar(50) DEFAULT NULL COMMENT '收件联系方式',
                                     `receive_address` varchar(500) DEFAULT NULL COMMENT '收件地址',
                                     `tracking_number` varchar(255) DEFAULT NULL COMMENT '快递单号',
                                     `express_company` varchar(100) DEFAULT NULL COMMENT '快递公司',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                     `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) DEFAULT NULL COMMENT '更新人',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                     `business_id` bigint(20) DEFAULT NULL COMMENT '项目id',
                                     `out_bill_no` varchar(64) DEFAULT NULL COMMENT '商户转账单号',
                                     `claim_status` int(1) DEFAULT '1' COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)',
                                     `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_activity_sign_code` (`activity_id`,`sign_code`),
                                     KEY `idx_result_status` (`result_status`),
                                     KEY `idx_prize_type` (`prize_type`),
                                     KEY `idx_activity_result_member` (`activity_id`,`result_status`,`member_id`),
                                     KEY `idx_activity_member_result_id` (`activity_id`,`member_id`,`result_status`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动记录表';

SET FOREIGN_KEY_CHECKS = 1;


    /*
 Navicat Premium Data Transfer

 Source Server         : 0090-local-业务库
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : 192.168.31.141:3307
 Source Schema         : cloud_0090_test

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 15/04/2026 13:03:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_help
-- ----------------------------
DROP TABLE IF EXISTS `activity_help`;
CREATE TABLE `activity_help` (
                                 `id` bigint(20) NOT NULL COMMENT '主键id',
                                 `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                 `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                 `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                 `help_date` date NOT NULL COMMENT '助力日期',
                                 `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                 `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                 `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_activity_id` (`activity_id`),
                                 KEY `idx_inviter_member_id` (`inviter_member_id`),
                                 KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_0
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_0`;
CREATE TABLE `activity_help_0` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_1
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_1`;
CREATE TABLE `activity_help_1` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_2
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_2`;
CREATE TABLE `activity_help_2` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_3
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_3`;
CREATE TABLE `activity_help_3` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_4
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_4`;
CREATE TABLE `activity_help_4` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_5
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_5`;
CREATE TABLE `activity_help_5` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_6
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_6`;
CREATE TABLE `activity_help_6` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_7
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_7`;
CREATE TABLE `activity_help_7` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_8
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_8`;
CREATE TABLE `activity_help_8` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_help_9
-- ----------------------------
DROP TABLE IF EXISTS `activity_help_9`;
CREATE TABLE `activity_help_9` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `inviter_member_id` bigint(20) NOT NULL COMMENT '邀请人会员ID',
                                   `invitee_member_id` bigint(20) NOT NULL COMMENT '被邀请人会员ID',
                                   `help_date` date NOT NULL COMMENT '助力日期',
                                   `scope_key` varchar(32) NOT NULL DEFAULT '' COMMENT '按天作用域key',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_activity_id` (`activity_id`),
                                   KEY `idx_inviter_member_id` (`inviter_member_id`),
                                   KEY `idx_invitee_member_id` (`invitee_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动分享助力记录表';

-- ----------------------------
-- Table structure for activity_task
-- ----------------------------
DROP TABLE IF EXISTS `activity_task`;
CREATE TABLE `activity_task` (
                                 `id` bigint(20) NOT NULL COMMENT '主键id',
                                 `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                 `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                 `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                 `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                 `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                 `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                 `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                 `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                 KEY `idx_member_id` (`member_id`),
                                 KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_0
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_0`;
CREATE TABLE `activity_task_0` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_1
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_1`;
CREATE TABLE `activity_task_1` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_2
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_2`;
CREATE TABLE `activity_task_2` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_3
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_3`;
CREATE TABLE `activity_task_3` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_4
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_4`;
CREATE TABLE `activity_task_4` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_5
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_5`;
CREATE TABLE `activity_task_5` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_6
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_6`;
CREATE TABLE `activity_task_6` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_7
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_7`;
CREATE TABLE `activity_task_7` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_8
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_8`;
CREATE TABLE `activity_task_8` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

-- ----------------------------
-- Table structure for activity_task_9
-- ----------------------------
DROP TABLE IF EXISTS `activity_task_9`;
CREATE TABLE `activity_task_9` (
                                   `id` bigint(20) NOT NULL COMMENT '主键id',
                                   `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
                                   `member_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `task_type` int(1) NOT NULL COMMENT '任务类型（1 签到 2 下单 3 分享 4 免费集签 5 浏览首页）',
                                   `finish_count` int(11) NOT NULL DEFAULT '0' COMMENT '任务完成数',
                                   `gain_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得抽签码次数',
                                   `consume_count` int(11) NOT NULL DEFAULT '0' COMMENT '消耗抽签码次数',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `creator` varchar(64) DEFAULT '' COMMENT '创建人',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新人',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_activity_member_task` (`activity_id`,`member_id`,`task_type`),
                                   KEY `idx_member_id` (`member_id`),
                                   KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='抽签活动任务完成情况';

SET FOREIGN_KEY_CHECKS = 1;
