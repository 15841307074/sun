--增加会员名称字段
ALTER TABLE lottery_log ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_0 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_1 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_2 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_3 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_4 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_5 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_6 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_7 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_8 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
ALTER TABLE lottery_log_9 ADD COLUMN member_name VARCHAR(255) COMMENT '会员名称';
--增加抽奖总次数
ALTER TABLE lottery_settings ADD COLUMN lottery_total_number int(11) NULL DEFAULT 0 COMMENT '抽奖总次数 0  无限制  其余 次数';
--增加抽奖方式
ALTER TABLE lottery_settings ADD COLUMN lottery_method int(1) NULL DEFAULT NULL COMMENT '抽奖方式 1 免费抽奖 2 积分抽奖 3 下单抽奖';
--增加支付门槛
ALTER TABLE lottery_settings ADD COLUMN payment_threshold decimal(11,2) NULL DEFAULT 0.00 COMMENT '支付门槛 0代表不限制';
--增加活动主表ID
ALTER TABLE lottery_settings ADD COLUMN activity_id bigint(20) NULL DEFAULT NULL COMMENT '活动主表ID';
--增加是否可转发选项
ALTER TABLE lottery_settings ADD COLUMN share_type int(11) NULL DEFAULT 2 COMMENT '1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友';
--增加可选商品类型
ALTER TABLE lottery_settings ADD COLUMN activity_product int(1) NULL DEFAULT 1 COMMENT '1 全部商品  2可选商品';
--增加活动背景图
ALTER TABLE lottery_settings ADD COLUMN `activity_background` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '活动背景图';
--增加按钮图
ALTER TABLE lottery_settings ADD COLUMN `button_img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '按钮图';
--增加颜色
ALTER TABLE lottery_settings ADD COLUMN `background_color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '颜色';
-- 新增广告抽奖类型
ALTER TABLE advertising_image ADD COLUMN `lottery_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '抽奖类型';
--增加保底次数
ALTER TABLE lottery_prize 
ADD COLUMN `minimum_number` int(11) NULL DEFAULT NULL COMMENT '保底次数';

ALTER TABLE lottery_prize 
ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';

ALTER TABLE lottery_prize ADD COLUMN store_id bigint(20) COMMENT '门店id';
ALTER TABLE lottery_prize ADD COLUMN coupon_name varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '优惠卷名称';


ALTER TABLE lottery_settings ADD COLUMN calculation_rules INT(1) NULL DEFAULT 1 COMMENT '计算规则(1 按天计算抽奖次数  2 按场次计算抽奖次数)';
ALTER TABLE lottery_settings ADD COLUMN prize_pool_rules INT(1) NULL DEFAULT 1 COMMENT '门店奖池规则(1 共用奖池  2 独立奖池)';
ALTER TABLE lottery_settings ADD COLUMN lottery_rules_reset INT(1) NULL DEFAULT 0 COMMENT '奖品及抽奖规则重置（0关闭 1开启）';

-- 适配活动主表
INSERT INTO activity (
    activity_name,
    participant_group,
    id,
    start_date,
    end_date,
    creator,
    updater,
    business_id,
    create_time,
    update_time,
		activity_type,
		activity_store,
		activity_rules,
		is_enabled
)
SELECT 
    lottery_title,
    1,
    id,
    lottery_start_time,
    lottery_end_time,
    creator,
    updater,
    business_id,
    create_time,
    update_time,
		3,
		1,
		lottery_rule,
		state
FROM lottery_settings where deleted = 0;
update lottery_settings set lottery_method = 2 where price is not null;
update lottery_settings set lottery_method = 3 where is_free=1;
update lottery_settings set activity_id = id;
update lottery_settings set lottery_total_number = lottery_limit;
update lottery_settings set share_type = 1;

-- 增加活动ID不等于空约束
ALTER TABLE lottery_settings 
MODIFY COLUMN `activity_id` bigint(20) NOT NULL COMMENT '活动ID';

-- 增加活动ID唯一索引
ALTER TABLE lottery_settings ADD UNIQUE INDEX uk_activity_id (activity_id);

-- 增加中奖城市字段大小
ALTER TABLE lottery_prize MODIFY COLUMN winning_citys VARCHAR(2000) CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '中奖城市区域范围  ，拼接';

-- 创建商品与抽奖活动关联表
CREATE TABLE `activity_lottery_commodity`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID',
  `commodity_id` bigint(20) NOT NULL COMMENT '商品ID',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `business_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_commodity_id`(`commodity_id`) USING BTREE,
  INDEX `fk_activity_commodity_activity`(`activity_id`) USING BTREE,
  INDEX `idx_business_id`(`business_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动关联商品表' ROW_FORMAT = Dynamic;

-- 刷新会员名称到抽奖记录表
update lottery_log_0 l,wx_member_0 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_1 l,wx_member_1 w set l.member_name = w.member_nick_name   where l.member_id = w.member_id;
update lottery_log_2 l,wx_member_2 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_3 l,wx_member_3 w set l.member_name = w.member_nick_name   where l.member_id = w.member_id;
update lottery_log_4 l,wx_member_4 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_5 l,wx_member_5 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_6 l,wx_member_6 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_7 l,wx_member_7 w set l.member_name = w.member_nick_name   where l.member_id = w.member_id;
update lottery_log_8 l,wx_member_8 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;
update lottery_log_9 l,wx_member_9 w set l.member_name = w.member_nick_name  where l.member_id = w.member_id;




-- 转账记录表
CREATE TABLE lottery_transfer_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    out_bill_no VARCHAR(64) NOT NULL UNIQUE COMMENT '商户转账单号',
    transfer_bill_no VARCHAR(64) COMMENT '微信转账单号',
    app_id VARCHAR(32) NOT NULL COMMENT '小程序AppID',
    open_id VARCHAR(64) NOT NULL COMMENT '用户OpenID',
    user_name VARCHAR(64) COMMENT '用户姓名(加密)',
    transfer_amount BIGINT NOT NULL COMMENT '转账金额(分)',
    transfer_remark VARCHAR(255) COMMENT '转账备注',
    transfer_scene_id VARCHAR(20) NOT NULL COMMENT '转账场景ID',
    user_recv_perception VARCHAR(100) COMMENT '用户收款感知',
    notify_url VARCHAR(255) COMMENT '回调地址',
    status VARCHAR(20) DEFAULT 'INIT' COMMENT '状态: INIT-初始, ACCEPTED-已受理, PROCESSING-处理中, WAIT_USER_CONFIRM-等待用户确认, SUCCESS-成功, FAIL-失败',
    package_info TEXT COMMENT '调起支付的参数',
    fail_reason TEXT COMMENT '失败原因',
    creator varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
    create_time timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    update_time timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    business_id bigint(20) NULL DEFAULT NULL,
    INDEX idx_openid (open_id),
    INDEX idx_out_bill_no (out_bill_no),
    INDEX idx_status (status)
);

-- 转账场景报告信息表
CREATE TABLE lottery_transfer_scene_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    out_bill_no VARCHAR(64) NOT NULL COMMENT '关联转账单号',
    info_type VARCHAR(50) NOT NULL COMMENT '信息类型',
    info_content VARCHAR(255) NOT NULL COMMENT '信息内容',
    creator varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
    create_time timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    update_time timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    deleted bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    business_id bigint(20) NULL DEFAULT NULL,
    INDEX idx_out_bill_no (out_bill_no)
);

ALTER TABLE lottery_log ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_0 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_1 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_2 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_3 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_4 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_5 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_6 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_7 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_8 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';
ALTER TABLE lottery_log_9 ADD COLUMN out_bill_no VARCHAR(64) COMMENT '商户转账单号';



ALTER TABLE lottery_log ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_0 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_1 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_2 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_3 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_4 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_5 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_6 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_7 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_8 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';
ALTER TABLE lottery_log_9 ADD COLUMN claim_status INT(1) NULL DEFAULT 1 COMMENT '红包领取状态(1 未领取  2 已领取  3已过期)';


ALTER TABLE lottery_log ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_0 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_1 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_2 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_3 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_4 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_5 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_6 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_7 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_8 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';
ALTER TABLE lottery_log_9 ADD COLUMN `code` varchar(255) NULL DEFAULT NULL COMMENT '编码';

ALTER TABLE lottery_log ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_0 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_1 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_2 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_3 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_4 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_5 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_6 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_7 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_8 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';
ALTER TABLE lottery_log_9 ADD COLUMN `price` int(10) DEFAULT NULL COMMENT '抽奖积分价格';


ALTER TABLE lottery_log ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_0 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_1 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_2 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_3 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_4 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_5 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_6 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_7 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_8 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_log_9 ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';
ALTER TABLE lottery_prize ADD COLUMN `store_id` bigint(20) DEFAULT NULL COMMENT '门店id';

ALTER TABLE lottery_log ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_0 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_1 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_2 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_3 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_4 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_5 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_6 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_7 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_8 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';
ALTER TABLE lottery_log_9 ADD COLUMN `package_info` varchar(500) DEFAULT NULL COMMENT '红包返参链接';


ALTER TABLE lottery_prize MODIFY remain_num INT DEFAULT 0;
update lottery_prize set remain_num = 0 where remain_num is null;


ALTER TABLE lottery_settings ADD COLUMN public_button INT(1) DEFAULT 0 COMMENT '公共展示（0开启  1 关闭）';