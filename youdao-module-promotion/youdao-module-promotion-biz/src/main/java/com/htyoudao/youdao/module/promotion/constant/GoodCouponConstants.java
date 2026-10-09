package com.htyoudao.youdao.module.promotion.constant;

/**
 * @author 33483
 */
public interface GoodCouponConstants {

    /**
     * 有效期是时间段
     */
    Integer USE_TYPE_0 = 0;

    /**
     * 立即生效
     */
    Integer USE_TYPE_1 = 1;

    /**
     * 领券后N天生效
     */
    Integer USE_TYPE_2 = 2;

    /**
     * 指定周几失效
     */
    Integer USE_TYPE_3 = 3;

    /**
     * 自动发放
     */
    Integer DISTRIBUTE_METHOD_0 = 0;

    /**
     * 手动领取
     */
    Integer DISTRIBUTE_METHOD_1 = 1;

    Long LONG_DISTRIBUTE_METHOD_1 = 1L;

    Integer IS_COMMON_1 = 1;

    Integer IS_COMMON_2 = 2;


    /**
     * 1 商品通用
     */
    Integer IS_COMMON_STORE_1 = 1;

    /**
     * 2 指定商品可用
     */
    Integer IS_COMMON_STORE_2 = 2;

    /**
     * 3 指定商品不可用
     */
    Integer IS_COMMON_STORE_3 = 3;

    /**
     * 优惠券下架
     */
    Integer IS_GROUND_0 = 0;

    /**
     * 优惠券已上架
     */
    Integer IS_GROUND_1 = 1;

    Integer DAY_LIMIT_0 = 0;

    String COUPON_CODE_PREFIX = "COUPON_";

    Integer USER_RESTRICTIONS_0 = 0;

    Integer USER_RESTRICTIONS_1 = 1;

    Integer USER_RESTRICTIONS_2 = 2;

    Integer USER_RESTRICTIONS_3 = 3;

    Integer USER_RESTRICTIONS_4 = 4;

    Integer USER_RESTRICTIONS_5 = 5;

    /**
     * 优惠券门店数量的key
     */
    String COUPON_STORE_NUM = "COUPON_STORE_NUM:";

    /**
     * 优惠券详情的key
     */
    String COUPON_DETAIL = "COUPON_DETAIL:";

    /**
     * 优惠券已领取数量的key
     */
    String COUPON_CLAIMED_NUM = "COUPON_CLAIMED_NUM:";


    /**
     * 优惠券门店数量不限制
     */
    Integer STORE_LIMIT_0 = 0;

    /**
     * 优惠券包已领取数量的key
     */
    String COUPON_PACKAGE_CLAIMED_NUM = "COUPON_PACKAGE_CLAIMED_NUM:";

    /**
     * 不能转发
     */
    Integer IS_SHARE_0 = 0;

    /**
     * 剩余数量不可见
     */
    Integer COUPON_NUM_VISIBLE_1 = 1;

    /**
     * 领取时间限制 0 不限制
     */
    Integer CLAIM_TIME_LIMIT_0 = 0;

    /**
     * 领取时间限制 1 限制
     */
    Integer CLAIM_TIME_LIMIT_1 = 1;



    /**
     * 普通满减券
     */
    String COUPON_TYPE_0 = "0";


    /**
     * 普通折扣券
     */
    String COUPON_TYPE_1 = "1";


    /**
     * 普通兑换券
     */
    String COUPON_TYPE_2 = "2";


    /**
     * 抖音满减券
     */
    String COUPON_TYPE_3 = "3";

    /**
     * 抖音兑换券
     */
    String COUPON_TYPE_4 = "4";

    /**
     * 商品券
     */
    Integer COUPON_TYPE_5 = 5;



    /**
     * 抖音券
     */
    String USE_RULE_1 = "1";

    Integer EXCHANGE_FLAG_1 = 1;

    Integer EXCHANGE_FLAG_0 = 0;

    /**
     * 不绑定门店标签
     */
    Integer STORE_TAG_FLAG_1 = 1;

    /**
     * 绑定门店标签
     */
    Integer STORE_TAG_FLAG_0 = 0;
}
