package com.htyoudao.youdao.framework.common.constants;

/**
 * <p>
 * 框架级别的Redis Key常量， 业务模块的KEY写在各自的模块中的RedisKeyConstants
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-13
 */
public interface RedisKeyConstants {

    /**
     * 用于生成全局唯一workerID，雪花ID使用
     */
    String SAAS_SNOWFLAKE_WORKERID = "saas_snowflake_workerId:";

    /**
     * 商品销量
     */
    String SAAS_PRODUCT_SALES = "saas_product_sales";

    /**
     * 加购商品销量
     */
    String SAAS_PURCHASE_PRODUCT_SALES = "saas_purchase_product_sales";

    /**
     * 集点缓存
     */
    String JD_MEMBER_POINTS_COLLECT = "activity_jd_points_collcet:";

    /**
     * 集点活动
     */
    String JD_ACTIVITY = "activity_jd:";

    /**
     * 集点活动单活动领取数量
     */
    String JD_ACTIVITY_CLAIMED_NUM = "activity_jd_claimed_num:";

    /**
     * 集点活动的会员
     */
    String JD_MEMBER = ":member_jd:";

    /**
     * 集点活动的兑换物
     */
    String JD_COUPON = "coupon_jd:";

    /**
     * 门店参与的集点活动
     */
    String JD_ACTIVITY_STORE = "activity_jd_store:";

    /**
     * 集点活动兑换物品优惠券
     */
    String JD_ACTIVITY_GOODS_COUPON = "activity_jd_goods_coupon:";

    /**
     * 集点活动兑换物品优惠券包
     */
    String JD_ACTIVITY_GOODS_COUPON_PACKAGE = "activity_jd_goods_coupon_package:";

    /**
     * 订单活动是否存在
     */
    String ACTIVITY_EXIST_KEY = "activity_exist:";

    /**
     * 注册开关 Hash
     */
    String REQ_HASH_KEY = "ylb:req:store";

    /**
     * 推送开关 Hash
     */
    String PUSH_HASH_KEY = "ylb:push:store";

}
