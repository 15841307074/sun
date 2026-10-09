package com.htyoudao.youdao.module.member.api.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * infra 系统，使用 1-001-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== AUTH 模块 1-004-000-000 ==========
    ErrorCode AUTH_LOGIN_ALIPAY_FAIL = new ErrorCode(1_004_000_000, "支付宝登录失败");
    ErrorCode AUTH_LOGIN_WECHAT_FAIL = new ErrorCode(1_004_000_001, "微信登录失败");
    ErrorCode AUTH_SIGN_ALIPAY_FAIL = new ErrorCode(1_004_000_003, "支付宝验签失败");
    ErrorCode AUTH_ENCRYPT_ALIPAY_FAIL = new ErrorCode(1_004_000_004, "支付宝加解密失败");
    ErrorCode AUTH_INFO_WECHAT_FAIL = new ErrorCode(1_004_000_005, "微信获取手机号失败");

    // ========== 会员模块 1-004-001-000 ==========
    ErrorCode WX_MEMBER_NOT_EXISTS = new ErrorCode(1_004_001_000, "会员不存在");

    ErrorCode WX_MEMBER_REJECT_UPDATE = new ErrorCode(1_004_001_001, "未传入 openid memberId , 拒绝修改");
    ErrorCode WX_MEMBER_NAME_ERROR = new ErrorCode(1_004_001_002, "昵称包含敏感词");
    ErrorCode WX_MEMBER_ORDER_AVG_TIME = new ErrorCode(1_004_001_003, "下单时间与单均实付必须同时选择");
    ErrorCode WX_MEMBER_GET_LIMITER = new ErrorCode(1_004_001_004, "抱歉!请求过于频繁，请稍后再试!");
    ErrorCode WX_MEMBER_ORDER_FREQUENCY = new ErrorCode(1_004_001_005, "下单频次时间与下单频次必须同时选择");
    ErrorCode WX_MEMBER_REJECT_QUERY = new ErrorCode(1_004_001_006, "下单频次时间与下单频次必须同时选择");

    ErrorCode WX_MEMBER_STOREID_ERROR = new ErrorCode(1_004_001_007, "当前门店信息异常，请重新选择");
    ErrorCode WX_MEMBER_GET_MOBILE_ERROR = new ErrorCode(1_004_001_008, "手机号不唯一，无法发放");

    ErrorCode WX_MEMBER_MOBILE_BINDING_ERROR = new ErrorCode(1_004_001_009, "用户手机号换次数绑达到上限");
    ErrorCode WX_MEMBER_FALLBACK_ERROR = new ErrorCode(1_004_001_011, "系统繁忙，请稍后重试");
    ErrorCode WX_MEMBER_BLOCK_ERROR = new ErrorCode(1_002_031_012, "系统繁忙，请重试");
    ErrorCode WX_MEMBER_ADDRESS_DETAIL_CONTAINS_EMOJI_ERROR = new ErrorCode(1_004_001_013, "用户地址包含表情");
    ErrorCode WX_MEMBER_IS_STORE_NOT_NULL = new ErrorCode(1_004_001_014, "isStore和deptId要么都空，要么都不空");
    ErrorCode WX_MEMBER_PAGE_NO_LIMIT = new ErrorCode(1_004_001_015, "查询页码不能大于1000页");
    ErrorCode WX_MEMBER_PHONE_CHANGE_ERROR = new ErrorCode(1_004_001_016, "手机号换绑失败，重新换绑");

    ErrorCode WX_MEMBER_ID_NOT_EXISTS = new ErrorCode(1_004_001_016, "会员ID不能为空");
    ErrorCode WX_MEMBER_IS_EMPTY = new ErrorCode(1_004_001_017, "手机号不能为空");

    ErrorCode WX_MEMBER_ID_IS_EMPTY = new ErrorCode(1_004_001_018, "用户ID不能为空");
    ErrorCode WX_MEMBER_SEND_COUPON_SIZE = new ErrorCode(1_004_001_019, "用户批量发券上线为500人");
    ErrorCode WX_MEMBER_EXPORT_TIME_ERROR = new ErrorCode(1_004_001_020, "当前时间{}不允许访问该接口，请于{}后再试");

    ErrorCode WX_MEMBER_ID_ERROR = new ErrorCode(1_004_001_021, "用户异常");

    ErrorCode WX_MEMBER_NAME_LENGTH_ERROR = new ErrorCode(1_004_001_022, "用户名长度有误");

    ErrorCode WX_MEMBER_NICK_NAME_LENGTH_ERROR = new ErrorCode(1_004_001_023, "用户昵称长度有误");

    ErrorCode WX_MEMBER_AVATAR_LENGTH_ERROR = new ErrorCode(1_004_001_024, "头像地址长度有误");

    ErrorCode WX_MEMBER_GENDER_ERROR = new ErrorCode(1_004_001_025, "用户性别有误");

    ErrorCode WX_MEMBER_OPENID_ERROR = new ErrorCode(1_004_001_026, "用户微信标识有误");

    ErrorCode WX_MEMBER_AVATAR_ERROR = new ErrorCode(1_004_001_027, "头像有误");

    ErrorCode WX_MEMBER_CHANGE_BINDING_ERROR = new ErrorCode(1_004_001_028, "换绑有误");

    ErrorCode WX_MEMBER_BINDING_COUNT_ERROR = new ErrorCode(1_004_001_029, "绑定有误");

    // ========== sharding模块 1-004-002-000 ==========
    ErrorCode WX_MEMBER_SHARDING_VALUE_FAIL = new ErrorCode(1_004_002_000, "获取分表字段失败");

    // ========== map模块 1-004-003-000===========
    ErrorCode WX_MEMBER_MAP_GET = new ErrorCode(1_004_003_000, "获取失败");





    ErrorCode POINTS_PRODUCT_NOT = new ErrorCode(1_004_004_001, "商品价格不能为负数");

    ErrorCode POINTS_PRODUCT_NOT_LING = new ErrorCode(1_004_004_002, "商品价格不能为0元");


    ErrorCode POINTS_PRODUCT_NOT_SORT = new ErrorCode(1_004_004_003, "商品排序不能为负数");

    ErrorCode POINTS_PRODUCT_NOT_NAME = new ErrorCode(1_004_004_004, "商品名称不能为空");

    ErrorCode POINTS_PRODUCT_NOT_S = new ErrorCode(1_004_004_005, "商品排序不能为空");

    ErrorCode POINTS_PRODUCT_NOT_F = new ErrorCode(1_004_004_006, "商品类型不能为空");
    ErrorCode POINTS_PRODUCT_NOT_K = new ErrorCode(1_004_004_007, "商品库存不能为空");
    ErrorCode POINTS_PRODUCT_NOT_J = new ErrorCode(1_004_004_008, "商品价格不能为空");

    ErrorCode POINTS_PRODUCT_ERROR_NAME = new ErrorCode(1_004_004_009, "商品名称不能重复");

    ErrorCode POINTS_PRODUCT_ID_NOT_NULL = new ErrorCode(1_004_004_010, "商品ID不能为空");

    // ========== 积分兑换 1-005-001-000 ==========
    ErrorCode POINTS_PRODUCT_NOT_E = new ErrorCode(1_005_001_001, "积分不足");
    ErrorCode POINTS_PRODUCT_NOT_INVENTORY = new ErrorCode(1_005_001_002, "领取失败，库存不足");
    ErrorCode POINTS_PRODUCT_NOT_COUPON = new ErrorCode(1_005_001_003, "优惠券已领完");
    ErrorCode POINTS_EXCHANGE_DETAIL_ID_REQUIRED = new ErrorCode(1_005_001_004, "兑换记录ID和兑换记录编号不能同时为空");
    ErrorCode POINTS_EXCHANGE_RECORD_NOT_EXISTS = new ErrorCode(1_005_001_005, "积分商品兑换记录不存在");


    ErrorCode WX_MEMBER_CARD_ERROR = new ErrorCode(1_006_001_001, "会员卡名称已存在");
    ErrorCode WX_MEMBER_CARD_BENEFIT_NOT_EXISTS = new ErrorCode(1_006_001_002, "会员卡权益关联不存在");
    ErrorCode WX_MEMBER_CARD_BENEFIT_DUPLICATE = new ErrorCode(1_006_001_003, "当前会员卡已绑定该权益");
    ErrorCode WX_MEMBER_CARD_BENEFIT_SCENE_ERROR = new ErrorCode(1_006_001_004, "权益场景不正确");
    ErrorCode WX_MEMBER_CARD_BENEFIT_COUPON_TYPE_ERROR = new ErrorCode(1_006_001_005, "券类型不正确");
    ErrorCode WX_MEMBER_CARD_BENEFIT_REPEAT_TYPE_ERROR = new ErrorCode(1_006_001_006, "重复周期不正确");
    ErrorCode WX_MEMBER_CARD_BENEFIT_ISSUE_VALUE_ERROR = new ErrorCode(1_006_001_007, "发放日期值不正确");
    ErrorCode WX_MEMBER_CARD_BENEFIT_MEMBER_CARD_NOT_EXISTS = new ErrorCode(1_006_001_008, "会员卡不存在");
    ErrorCode WX_MEMBER_CARD_BENEFIT_COUPON_ID_ERROR = new ErrorCode(1_006_001_009, "券/券包ID不能为空");
    ErrorCode WX_MEMBER_CARD_BENEFIT_SEND_NUM_ERROR = new ErrorCode(1_006_001_010, "发放数量必须大于0");
    ErrorCode WX_MEMBER_CARD_POINTS_RANGE_CONFLICT = new ErrorCode(1_006_001_011, "会员卡积分区间冲突");
    ErrorCode WX_MEMBER_CARD_MEMBER_BENEFIT_TOTAL_SEND_NUM_ERROR = new ErrorCode(1_006_001_012, "升级权益中优惠券发放总张数不能超过5张");
    ErrorCode WX_MEMBER_CARD_BIRTHDAY_BENEFIT_TOTAL_SEND_NUM_ERROR = new ErrorCode(1_006_001_013, "生日礼包中优惠券发放总张数不能超过5张");
    ErrorCode WX_MEMBER_CARD_MEMBER_BENEFIT_PACKAGE_ERROR = new ErrorCode(1_006_001_014, "升级权益中不能配置优惠券包");
    ErrorCode WX_MEMBER_CARD_BIRTHDAY_BENEFIT_PACKAGE_ERROR = new ErrorCode(1_006_001_015, "生日礼包中不能配置优惠券包");
    ErrorCode WX_MEMBER_CARD_UPGRADE_BENEFIT_TOTAL_SEND_NUM_ERROR = new ErrorCode(1_006_001_016, "升级权益中优惠券发放总张数不能超过5张");
    ErrorCode WX_MEMBER_CARD_UPGRADE_BENEFIT_PACKAGE_ERROR = new ErrorCode(1_006_001_017, "升级权益中不能配置优惠券包");
    ErrorCode POINTS_PAGE_NOT_E = new ErrorCode(1_005_006_002, "查询页数不能超过5000页");
    ErrorCode POINTS_PAGE_1000_NOT_E = new ErrorCode(1_005_006_003, "查询页数不能超过1000页");
    ErrorCode LOTTERY_PAGE_1000_NOT_E = new ErrorCode(1_005_006_003, "查询页数不能超过1000页");

    // ========== 会员标签 1_007_001_000 ==========
    ErrorCode WX_MEMBER_TAG_VALUE_EXISTS = new ErrorCode(1_007_001_000, "已有会员使用，请勿删除");

    ErrorCode WX_MEMBER_TAG_VALUE_NOT_EXISTS = new ErrorCode(1_007_001_001, "标签不存在");

    ErrorCode WX_MEMBER_TAG_GROUP_NOT_EXISTS = new ErrorCode(1_007_001_002, "标签组不存在");

    ErrorCode WX_MEMBER_TAG_GROUP_EXISTS = new ErrorCode(1_007_001_003, "标签组已存在");

    // ========== 自定义人群 1_008_001_000 ==========
    ErrorCode CROWD_NOT_EXISTS = new ErrorCode(1_008_001_001, "自定义人群不存在");
    ErrorCode GOODCOUPON_CROWD_EXISTS = new ErrorCode(1_008_001_002, "已经被优惠券使用，无法删除!");

    ErrorCode MEMBER_CROWD_BIRTHDAY_NULL_ERROR = new ErrorCode(1_008_001_002, "人群生日异常");
    ErrorCode MEMBER_CROWD_BIRTHDAY_FORMAT_ERROR = new ErrorCode(1_008_001_002, "人群生日格式异常");

    ErrorCode GOODCOUPON_CROWD_TOTAL_ERROR = new ErrorCode(1_008_001_005, "自定义人群最多可创建20个，请删除已有人群后再试!");


    // ========== 交易密码模块 1-004-008-000 ==========
    ErrorCode TRAN_PASSWORD_NOT_MATCH = new ErrorCode(1_004_008_001, "两次输入的密码不一致");
    ErrorCode TRAN_PASSWORD_FORMAT_ERROR = new ErrorCode(1_004_008_002, "交易密码必须为6位数字");
    ErrorCode TRAN_PASSWORD_ALREADY_SET = new ErrorCode(1_004_008_003, "交易密码已设置，请勿重复设置");
    ErrorCode TRAN_PASSWORD_NOT_SET = new ErrorCode(1_004_008_004, "交易密码未设置");
    ErrorCode TRAN_PASSWORD_ERROR = new ErrorCode(1_004_008_005, "交易密码错误");
    ErrorCode TRAN_PASSWORD_LOCKED = new ErrorCode(1_004_008_006, "交易密码错误次数过多，请{}分钟后再试");
    ErrorCode TRAN_PASSWORD_SET_FAIL = new ErrorCode(1_004_008_007, "设置交易密码失败");
    ErrorCode OLD_PASSWORD_ERROR = new ErrorCode(1_004_008_008, "原密码错误");
    ErrorCode UPDATE_PASSWORD_FAIL = new ErrorCode(1_004_008_009, "修改交易密码失败");
    ErrorCode FORGET_PASSWORD_FAIL = new ErrorCode(1_004_008_010, "找回密码失败");
    ErrorCode SMS_CODE_ERROR = new ErrorCode(1_004_008_011, "验证码错误或已失效");

}
