package com.htyoudao.youdao.module.order.dal.redis;


/**
 * member Redis Key 枚举类
 *
 * @author 0090
 */
public interface RedisKeyConstants {

    /**
     * 订单结算缓存
     * KEY 格式：saas_order_calc_cache:{memberId}
     * VALUE 数据格式 String
     */
    String ORDER_CALC_CACHE = "saas_order_calc_cache:";

    /**
     * 订单提交缓存
     * KEY 格式：saas_order_submit_cache:{orderSn}
     * VALUE 数据格式 String
     */
    String ORDER_SUBMIT_CACHE = "saas_order_submit_cache:";

    /**
     * 用户地址
     * KEY 格式：saas_order_submit_token:{memberId}
     * VALUE 数据格式 String
     */
    String ORDER_SUBMIT_TOKEN = "saas_order_submit_token:";

    /**
     * 拼单业务 长连接用户Session前缀
     */
    String SESSION_KEY_PREFIX = "saas_splicing_session:";

    /**
     * 订单锁定标识
     */
    String SPLICING_LOCK_STATUS = "saas_splicing_lock_status:";

    /**
     * 订单取餐码
     */
    String PICKUP_CODE_KEY = "saas_pick_up_code_store:%s:date:%s:code";

    /**
     * 订单外卖取餐码
     */
    String WM_PICKUP_CODE_KEY ="saas_wm_order_code_store:%s:date:%s:code";

    /**
     * 订单代取取餐码
     */
    String DQ_PICKUP_CODE_KEY = "saas_dq_order_code_store:%s:date:%s:code";

    /**
     * 订单取餐码锁
     */
    String PICKUP_CODE_LOCK_KEY = "saas_pick_up_code_lock:store:%s:date:%s";

    /**
     * 订单活动是否存在
     */
    String ACTIVITY_EXIST_KEY = "activity_exist:";

    /**
     * seckill:user:buy:{activityId}:{sessionId}:{productId}:{userId} → integer（用户已购买数；设置 TTL 到活动结束）
     */
    String SECKILL_USER_BUY_KEY = "seckill:{%s}:user:buy:%s:%s:%s";

    /**
     * seckill:store:buy:{activityId}:{sessionId}:{storeId} → integer（门店下单次数）
     */
    String SECKILL_STORE_BUY_KEY = "seckill:{%s}:store:buy:%s:%s";

    /**
     * 秒杀库存 KEY seckill:stock:{storeId}:{activityId}:{productId}:{sessionId}
     */
    String SECKILL_STOCK_KEY = "seckill:{%s}:stock:%s:%s:%s";

    /**
     * 秒杀活动 KEY seckill:activity:{activityId}
     */
    String SECKILL_ACTIVITY_KEY= "seckill:activity:%s";

    /**
     * 秒杀活动并发计数 seckill:concurrent:{activityId}:{sessionId}
     */
    String SECKILL_CONCURRENT_KEY = "seckill:{%s}:concurrent:%s";

    /**
     * 排队队列  seckill:queue:{activityId}:{sessionId}
     */
    String SECKILL_QUEUE_KEY = "seckill:{%s}:queue:%s";

    /**
     * 转盘活动 memberID, LotteryID, 日期yyyy
     */
    String COMMODITY_LOTTERY_MEMBERID = "commodity_lottery_memberId:%s_%s_%s";

}
