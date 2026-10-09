package com.htyoudao.youdao.module.promotion.dal.redis;

public interface RedisKeyConstants {

    String LOTTERY_SETTING = "lottery_setting:";
    /**
     * 活动设置奖品缓存key
     */
    String LOTTERY_PRIZE = "lottery_prize:";

    /**
     * 活动设置奖品抽奖次数
     */
    String LOTTERY_NUM = "lottery_num:";
    /**
     * 活动设置奖品抽奖总次数
     */
    String LOTTERY_SUM = "lottery_sum:";
    /**
     * 活动设置人员抽奖次数总次数
     */
    String LOTTERY_MEMBER_NUM = "lottery_member_num:";
    /**
     * 获取抽奖商品次数
     */
    String PRIZE_NUM = "prize_num:";
    /**
     * 浏览任务完成次数
     */
    String LOTTERY_BROWSE_FINISHED_NUM = "lottery_browse_finished_num:";
    /**
     * 浏览任务额外可用次数
     */
    String LOTTERY_BROWSE_AVAILABLE_NUM = "lottery_browse_available_num:";
    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";
    /**
     * 转盘活动 memberID, LotteryID, 日期yyyy
     */
    String COMMODITY_LOTTERY_MEMBERID = "commodity_lottery_memberId:%s_%s_%s";

    /**
     * 有奖问答活动配置缓存
     */
    String ANSWER_SETTING = "activity_answer:setting:";
    /**
     * 有奖问答聚合配置缓存，结构参考抽奖 LotterySettingsCacheDataVO
     */
    String ANSWER_CACHE = "activity_answer:cache:";
    /**
     * 有奖问答题目缓存
     */
    String ANSWER_QUESTION = "activity_answer:questions:";
    /**
     * 有奖问答奖励缓存
     */
    String ANSWER_REWARD = "activity_answer:rewards:";
    /**
     * 有奖问答次数缓存
     */
    String ANSWER_NUM = "activity_answer:num:";
    /**
     * 有奖问答奖励库存缓存
     */
    String ANSWER_REWARD_STOCK = "activity_answer:reward_stock:";

    /**
     * 有奖问答任务完成次数缓存
     */
    String ANSWER_TASK = "activity_answer:task:";

    /**
     * 有奖问答当前场次记录短缓存
     */
    String ANSWER_CURRENT_RECORD = "activity_answer:current_record:";

    /**
     * 有奖问答当前记录题目明细短缓存
     */
    String ANSWER_RECORD_DETAIL = "activity_answer:record_detail:";

    /** 有奖问答小程序完整详情短缓存 */
    String ANSWER_APP_DETAIL = "activity_answer:app_detail:";

    /**
     * 有奖问答任务完成锁
     */
    String ANSWER_TASK_LOCK = "activity_answer:task_lock:";

    /**
     * 有奖问答会员信息短缓存
     */
    String ANSWER_MEMBER = "activity_answer:member:";

    /** 有奖问答社群参与资格短缓存 */
    String ANSWER_COMMUNITY = "activity_answer:community:";

    /** 有奖问答参与门店名称缓存 */
    String ANSWER_STORE_NAME = "activity_answer:store_name:";

    /**
     * 有奖问答提交/开始答题锁
     */
    String ANSWER_SUBMIT_LOCK = "activity_answer:submit_lock:";

    /**
     * 订单结算缓存
     * KEY 格式：saas_order_calc_cache:{memberId}
     * VALUE 数据格式 String
     */
    String ORDER_CALC_CACHE = "saas_order_calc_cache:";

    // ========== 问卷调查 ==========

    /**
     * 问卷详情缓存 KEY 格式：survey_detail:{surveyId}
     */
    String SURVEY_DETAIL = "survey_detail:";

    /**
     * 小程序问卷详情缓存（含题目选项）KEY 格式：survey_app_detail:{surveyId}
     */
    String SURVEY_APP_DETAIL = "survey_app_detail:";

    /**
     * 问卷UV（HyperLogLog）KEY 格式：survey_uv:{surveyId}
     */
    String SURVEY_UV = "survey_uv:";

    /**
     * 问卷UV手机号去重集合（Set）KEY 格式：survey_uv_phone:{surveyId}
     * 用于 UV 统计时判断手机号是否已统计过
     */
    String SURVEY_UV_PHONE = "survey_uv_phone:";

    /**
     * 问卷已提交手机号集合（Set）KEY 格式：survey_submitted:{surveyId}
     */
    String SURVEY_SUBMITTED = "survey_submitted:";

    /**
     * 问卷提交计数（AtomicLong）KEY 格式：survey_submit_count:{surveyId}
     */
    String SURVEY_SUBMIT_COUNT = "survey_submit_count:";

    /**
     * 手机号提交锁定（防并发重复）KEY 格式：survey_phone_lock:{surveyId}:{phone}
     */
    String SURVEY_PHONE_LOCK = "survey_phone_lock:";

    // ========== 满赠活动 ==========

    /**
     * 满赠活动赠品库存（共用库存模式）
     * KEY 格式：mz_gift:{activityId}
     * Hash 结构：field=giftCommodityId, value=剩余库存
     */
    String MZ_GIFT_INVENTORY = "mz_gift:";

    /**
     * 满赠活动赠品库存（独立库存模式，按门店）
     * KEY 格式：mz_gift:{activityId}:store:{storeId}
     * Hash 结构：field=giftCommodityId, value=剩余库存
     */
    String MZ_GIFT_STORE_INVENTORY = "mz_gift:%s:store:%s";

    /**
     * 满赠活动元数据缓存
     * KEY 格式：mz_activity:{activityId}
     * 缓存 giftInventoryType 等信息，用于判断走共用还是独立库存
     */
    String MZ_ACTIVITY = "mz_activity:";

    /**
     * 一级会员卡实际发放的优惠券 ID 集合（Set）
     *
     * KEY 格式：member_card_benefit:level_one:coupon_ids:{businessId}
     *
     * VALUE：十进制字符串格式的 couponId。
     *
     * 不设置 TTL：历史券即使之后被会员卡配置替换，归档任务仍需继续清理其超过 168 小时的用户券。
     */
    String MEMBER_CARD_LEVEL_ONE_COUPON_IDS = "member_card_benefit:level_one:coupon_ids:";

}
