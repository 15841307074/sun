package com.htyoudao.youdao.module.order.api.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * order 错误码枚举类
 * <p>
 * order 系统，使用 1_005_000_000 段
 */
public interface ErrorCodeConstants {

    //订单
    ErrorCode PAY_COMMON_EXCEPTION = new ErrorCode(1_005_001_000, "支付统一错误");
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(1_005_001_001, "订单不存在");
    ErrorCode ORDER_PICK_UP_NUM_BUSY_SERVICE = new ErrorCode(1_005_001_003, "获取取餐码拥挤，请重新下单");
    ErrorCode ORDER_WRONG_TYPE = new ErrorCode(1_005_001_004, "错误的订单类型");
    ErrorCode ORDER_WRONG_STATE = new ErrorCode(1_005_001_005, "错误的订单状态");
    ErrorCode ORDER_PACKAGE_INFORMATION_INCOMPLETE = new ErrorCode(1_005_001_006, "套餐信息不完整");
    ErrorCode ORDER_GET_AFTER_COMMODITY_FAIL = new ErrorCode(1_005_001_007, "加购商品数据加载失败");
    ErrorCode ORDER_GET_SINGLE_COMMODITY_FAIL = new ErrorCode(1_005_001_008, "套餐子品数据加载失败");
    ErrorCode ORDER_GET_CAR_COMMODITY_FAIL = new ErrorCode(1_005_001_009, "购物车信息为空");
    ErrorCode ORDER_CANNEL_REORDER_AGAIN = new ErrorCode(1_005_001_010, "订单已取消，请重新下单");
    ErrorCode ORDER_PAY_RECORD_NOT_EXISTS = new ErrorCode(1_005_001_011, "支付记录不存在");
    ErrorCode ORDER_NOT_CONFIG_PAY_CHANNEL_NO = new ErrorCode(1_005_001_012, "当前门店未配置支付渠道号");
    ErrorCode ORDER_HAS_PAYED = new ErrorCode(1_005_001_013, "订单已支付，请勿重复支付");
    ErrorCode ORDER_ZERO_ORDER_REFUND = new ErrorCode(1_005_001_014, "0元订单禁止退款");
    ErrorCode ORDER_SETTLEMENT_INFO_NOT_NULL = new ErrorCode(1_005_001_015, "结算信息不能为空");
    ErrorCode ORDER_CACHE_NOT_EXISTS = new ErrorCode(1_005_001_016, "缓存数据或商品信息为空");
    ErrorCode ORDER_CASH_ORDER_REFUND = new ErrorCode(1_005_001_017, "现金订单禁止退款");
    ErrorCode ORDER_WRITE_OFF_ERROR = new ErrorCode(1_005_001_018, "禁止夸门店核销");
    ErrorCode ORDER_AMOUNT_NOT_ENOUGH = new ErrorCode(1_005_001_019, "订单金额需大于等于门店起送费【%s元】");
    ErrorCode ORDER_SETTLEMENT_INFO_EXPIRE = new ErrorCode(1_005_001_020, "结算信息已失效,请返回点餐页重新选购");
    ErrorCode ORDER_SUBMIT_REPEAT = new ErrorCode(1_005_001_021, "请勿重复提交订单");
    ErrorCode ORDER_CALCULATION_INFO_ERROR = new ErrorCode(1_005_001_022, "不合规的结算信息");
    ErrorCode ORDER_PAGE_NOT_MORE = new ErrorCode(1_005_001_023, "页码不能超过5000页");
    ErrorCode ORDER_RE_PAY = new ErrorCode(1_005_001_024, "网络繁忙，请重新支付");
    ErrorCode ORDER_ERROR_SOURCE = new ErrorCode(1_005_001_025, "错误的订单提交来源");
    ErrorCode ORDER_COMMODITY_ALL_NOT_ALLOW_SEND = new ErrorCode(1_005_001_026, "该订单商品均为单点不送商品");
    ErrorCode ORDER_REMARK_LENGTH_ERROR = new ErrorCode(1_005_001_027, "订单备注长度限制140个字符");
    ErrorCode ORDER_PAYING_000000 = new ErrorCode(1_005_001_028, "订单已支付");
    ErrorCode ORDER_PAYING_222222 = new ErrorCode(1_005_001_029, "订单支付中");
    ErrorCode ORDER_ERRAND_ACCEPT_GENDER_LIMIT = new ErrorCode(1_005_001_030, "跑腿员性别不符合订单限制");
    ErrorCode ORDER_ERRAND_ACCEPTED_BY_OTHER = new ErrorCode(1_005_001_031, "订单已被其他跑腿员接单");
    ErrorCode ORDER_ERRAND_ACCEPT_CANCELED = new ErrorCode(1_005_001_032, "订单已取消");
    ErrorCode ORDER_ERRAND_ACCEPT_REFUNDED = new ErrorCode(1_005_001_033, "订单已退款");
    ErrorCode ORDER_ERRAND_ACCEPT_STATE_NOT_SUPPORT = new ErrorCode(1_005_001_034, "订单%s，不支持接单");
    ErrorCode ORDER_ERRAND_ACCEPT_LOCK_BUSY = new ErrorCode(1_005_001_035, "订单正在被其他跑腿员接单，请刷新后重试");
    ErrorCode ORDER_ERRAND_PICKUP_CANCELED = new ErrorCode(1_005_001_036, "该订单已取消");
    ErrorCode ORDER_ERRAND_PICKUP_REFUNDED = new ErrorCode(1_005_001_037, "该订单已退款");
    ErrorCode ORDER_ERRAND_PICKUP_STATE_NOT_SUPPORT = new ErrorCode(1_005_001_038, "该订单%s，不能取货");
    ErrorCode ORDER_ERRAND_DELIVERED_CANCELED = new ErrorCode(1_005_001_039, "该订单已取消");
    ErrorCode ORDER_ERRAND_DELIVERED_REFUNDED = new ErrorCode(1_005_001_040, "该订单已退款");
    ErrorCode ORDER_ERRAND_DELIVERED_STATE_NOT_SUPPORT = new ErrorCode(1_005_001_041, "该订单%s，不能确认送达");


    //优惠券
    ErrorCode ORDER_COUPON_ERROR = new ErrorCode(1_005_002_001, "优惠券被使用或已过期");

    //商品
    ErrorCode ORDER_COMMODITY_ERROR = new ErrorCode(1_005_003_001, "商品信息处理异常");
    ErrorCode ORDER_COMMODITY_BUY_NUMBER_ERROR = new ErrorCode(1_005_003_002, "商品【%s】最少购买【%s】份");
    ErrorCode ORDER_COMMODITY_SALE_RULE_ERROR = new ErrorCode(1_005_003_003, "商品【%s】%s");
    ErrorCode ORDER_COMMODITY_CONDIMENT_PARSE_ERROR = new ErrorCode(1_005_003_004, "商品【%s】小料数据解析失败");
    ErrorCode ORDER_COMMODITY_CONDIMENT_DOWN = new ErrorCode(1_005_003_005, "小料【%s】已下架");
    ErrorCode ORDER_COMMODITY_CONDIMENT_ID_ERROR = new ErrorCode(1_005_003_006, "无效的小料ID【%s】");
    ErrorCode ORDER_COMMODITY_AFTER_ID_ERROR = new ErrorCode(1_005_003_007, "加购商品ID缺失");
    ErrorCode ORDER_COMMODITY_SKU_ID_ERROR = new ErrorCode(1_005_003_008, "商品SKU_ID缺失");
    ErrorCode ORDER_COMMODITY_CONDIMENT_COPIES_ERROR = new ErrorCode(1_005_003_009, "商品【%s】小料不支持多选");
    ErrorCode ORDER_COMMODITY_CONDIMENT_UNMBER_ERROR = new ErrorCode(1_005_003_010, "商品【%s】小料数量超出【%s】份限制");
    ErrorCode ORDER_COMMODITY_IS_NOT_UP = new ErrorCode(1_005_003_011, "商品【%s】已下架");
    ErrorCode ORDER_COMMODITY_GET_FAIL = new ErrorCode(1_005_003_012, "商品数据为空");
    ErrorCode ORDER_COMMODITY_NOT_EXISTS = new ErrorCode(1_005_003_013, "部分商品已下架或修改");
    ErrorCode ORDER_COMMODITY_VALID_SINGLE= new ErrorCode(1_005_003_014, "部分套餐下分组商品已修改或已下架");
    ErrorCode ORDER_COMMODITY_ID_NOT_MATCH= new ErrorCode(1_005_003_015, "商品【%s】连锁库商品ID和SKUID不匹配");
    ErrorCode ORDER_COMMODITY_AFTER_REMOVE= new ErrorCode(1_005_003_016, "加购商品已被删除,请重新选购");
    ErrorCode ORDER_COMMODITY_CONDIMENT_BUY_NUMBER = new ErrorCode(1_005_003_017, "小料【%s】最多购买【%s】份");
    ErrorCode ORDER_COMMODITY_NOT_SELECT_ATTRIBUTE = new ErrorCode(1_005_003_018, "商品未配置任何可选属性，不能选择属性");
    ErrorCode ORDER_COMMODITY_PRODUCT_EMPTY = new ErrorCode(1_005_003_019, "订单商品不能为空");
    ErrorCode ORDER_EXCHANGE_COMMODITY_ONLY_COUPON = new ErrorCode(1_005_003_020, "仅兑换商品禁止单独售卖");

    //拼单
    ErrorCode ORDER_SPLICING_CANCELED = new ErrorCode(1_005_004_001, "拼单已取消");
    ErrorCode ORDER_SPLICING_DONE = new ErrorCode(1_005_004_002, "拼单已完结");
    ErrorCode ORDER_SPLICING_OVER = new ErrorCode(1_005_004_003, "拼单已结束");
    ErrorCode ORDER_SPLICING_PAYING = new ErrorCode(1_005_004_004, "拼单结算中");

    //门店
    ErrorCode ORDER_GET_STORE_FAIL = new ErrorCode(1_005_005_001, "获取门店信息失败");
    ErrorCode ORDER_STORE_NOT_OPEN = new ErrorCode(1_005_005_002, "门店已关店");
    ErrorCode ORDER_STORE_NOT_WORK = new ErrorCode(1_005_005_003, "门店已歇业");
    ErrorCode ORDER_STORE_NOT_WITHOUT_PAYMENT = new ErrorCode(1_005_005_004, "门店暂不支持不付款下单");
    ErrorCode ORDER_STORE_NOT_MINIPRO = new ErrorCode(1_005_005_005, "门店暂不支持小程序下单");
    ErrorCode ORDER_T_STORE_NOT_WORK = new ErrorCode(1_005_005_006, "堂食点单未在营业时间段内，请更换门店下单");
    ErrorCode ORDER_W_STORE_NOT_WORK = new ErrorCode(1_005_005_007, "外卖点单未在营业时间段内，请更换门店下单");
    ErrorCode ORDER_STORE_WITHOUT_PAYMENT = new ErrorCode(1_005_005_008, "门店暂不支持付款下单");
    ErrorCode ORDER_STORE_WITHOUT_DELIVERY = new ErrorCode(1_005_005_009, "门店暂不支持外卖下单");

    //拼单
    ErrorCode ORDER_SPLICING_MAIN_ID_ERROR = new ErrorCode(1_005_006_001, "拼单主体ID不能为空");
    ErrorCode ORDER_SPLICING_ONLY_ONE_ERROR = new ErrorCode(1_005_006_002, "该拼单正在其他终端登录");
    ErrorCode ORDER_SPLICING_CANCELED_ERROR = new ErrorCode(1_005_006_003, "该拼单已取消");
    ErrorCode ORDER_SPLICING_DONE_ERROR = new ErrorCode(1_005_005_004, "该拼单已完结");
    ErrorCode ORDER_SPLICING_NOT_ALLOW_COUPON = new ErrorCode(1_005_005_005, "拼单禁止使用优惠券");
    ErrorCode ORDER_SPLICING_NOT_IN_MAIN = new ErrorCode(1_005_005_006, "您已被移除当前拼单");

    //云喇叭
    ErrorCode ORDER_YLB_STORE_REGISTER_FAIL = new ErrorCode(1_005_006_001, "门店注册失败，稍后重试");
    ErrorCode ORDER_YLB_CANNOT_USE = new ErrorCode(1_005_006_002, "请联系管理员开通权限");
    ErrorCode ORDER_YLB_ID_NOT_OWN = new ErrorCode(1_005_006_003, "开发者帐号ID非本平台所有");

    //营销
    ErrorCode ORDER_GET_ACTIVITY_FAIL = new ErrorCode(1_005_007_001, "活动数据加载失败");
    ErrorCode ORDER_ACTIVITY_NOT_IN_GROUP = new ErrorCode(1_005_007_002, "未进社群禁止参加活动");

    //活动
    ErrorCode ORDER_ACTIVITY_TYPE_ERROR = new ErrorCode(1_005_008_001, "不合法的营销活动类型");
    ErrorCode ORDER_ACTIVITY_CLAC_ERROR = new ErrorCode(1_005_008_002, "查找活动最优金额失败");
    ErrorCode ORDER_ACTIVITY_CAN_NOT_MATCH_MORE = new ErrorCode(1_005_008_003, "单商品暂不支持命中多种优惠活动");
    ErrorCode ORDER_ACTIVITY_TOO_MANY_COMMODITY = new ErrorCode(1_005_008_004, "参与活动的品较多，请分批下单");

    //权限
    ErrorCode DATA_PERMISSION_IS_EMPTY = new ErrorCode(1_005_009_001, "数据权限为空");

    //秒杀
    ErrorCode ORDER_SHOP_NOT_PARTICIPATE = new ErrorCode(1_005_010_001, "当前门店未参加本次活动，请更换门店");
    ErrorCode ORDER_SECKILL_END = new ErrorCode(1_005_010_002, "活动已结束，下次记得早点来哦~");
    ErrorCode ORDER_SESSION_NOT_START = new ErrorCode(1_005_010_003, "当前场次未开始或已结束");
    ErrorCode ORDER_TOO_MANY_REQUEST = new ErrorCode(1_005_010_004, "前方拥堵，请稍后重试~");
    ErrorCode ORDER_LINE_UP = new ErrorCode(1_005_010_005, "排队中，前面还有%s人~");
    ErrorCode ORDER_MORE_THAN_LIMIT = new ErrorCode(1_005_010_006, "当前门店活动名额已满，请更换门店下单");
    ErrorCode ORDER_USER_MORE_THAN_LIMIT = new ErrorCode(1_005_010_007, "商品%s最多可购买%s份,请重新下单");
    ErrorCode ORDER_COMMODITY_STOCK_NOT_ENOUGH = new ErrorCode(1_005_010_008, "商品%库存不足,请重新下单");
    ErrorCode ORDER_SECKILL_INFO_NOT_EXIST= new ErrorCode(1_005_010_009, "秒杀信息不能为空");
    ErrorCode ORDER_GET_SECKILL_ACTIVITY_FAIL = new ErrorCode(1_005_005_010, "获取秒杀商品信息失败");
    ErrorCode ORDER_SECKILL_SKU_ERROR = new ErrorCode(1_005_005_011, "获取秒杀商品规格异常");
    ErrorCode ORDER_SECKILL_ORDER_ERROR = new ErrorCode(1_005_005_012, "秒杀活动抢单异常");

    String LOG_SYSTEM = "订单";
    String LOG_ORDER_REFUND_SUB_TYPE = "PC发起了退款";
    String LOG_ORDER_REFUND_SUCCESS = "PC发起了退款,订单号【{{#bzOrderDO.orderSn}}】";
}
