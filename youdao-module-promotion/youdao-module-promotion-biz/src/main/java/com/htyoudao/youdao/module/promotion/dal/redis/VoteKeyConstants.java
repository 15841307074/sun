package com.htyoudao.youdao.module.promotion.dal.redis;

/**
 * 投票活动 Redis Key 常量
 */
public interface VoteKeyConstants {

    /** 投票活动配置缓存 vote_setting:{activityId} */
    String VOTE_SETTING = "vote_setting:";

    /** 投票选项列表缓存 vote_options:{activityId} */
    String VOTE_OPTIONS = "vote_options:";

    /** 选项票数计数 vote_option_count:{activityId}:{optionId} */
    String VOTE_OPTION_COUNT = "vote_option_count:";

    /** 用户投票次数（活动期间总，手机号标识） vote_member_total:{activityId}:{mobile} */
    String VOTE_MEMBER_TOTAL = "vote_member_total:";

    /** 用户每日投票次数（手机号标识） vote_member_daily:{activityId}:{mobile}:{yyyyMMdd} */
    String VOTE_MEMBER_DAILY = "vote_member_daily:";

    /** 用户是否已领取奖励（手机号标识） vote_reward_claimed:{activityId}:{mobile} */
    String VOTE_REWARD_CLAIMED = "vote_reward_claimed:";

    /** 活动奖励配置缓存 vote_reward_config:{activityId} */
    String VOTE_REWARD_CONFIG = "vote_reward_config:";

    /** 奖励库存 vote_reward_stock:{rewardId} */
    String VOTE_REWARD_STOCK = "vote_reward_stock:";

    /** 活动门店Set vote_store:{activityId}，member=storeId */
    String VOTE_STORE = "vote_store:";

    /** 分布式锁前缀 */
    String VOTE_LOCK_PREFIX = "lock:vote:";
}
