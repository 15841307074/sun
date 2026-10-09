package com.htyoudao.youdao.module.promotion.dal.redis;

public interface JKKeyConstants {

    /**
     * 集卡活动配置缓存。
     */
    String JK_SETTING = "jk_setting:";

    /**
     * 集卡活动奖品缓存。
     */
    String JK_PRIZE = "jk_prize:";

    /**
     * 集卡活动卡片缓存。
     */
    String JK_CARD = "jk_card:";

    /**
     * 集卡助力记录缓存。
     */
    String JK_HELP = "jk_help:";

    /**
     * 用户累计可用抽卡次数缓存。
     * 适用于不限当天次数的任务奖励。
     */
    String JK_DRAW_CHANCE_UNLIMITED = "jk_draw_chance_unlimited:";

    /**
     * 用户当日可用抽卡次数缓存。
     * 适用于限制当天次数的任务奖励。
     */
    String JK_DRAW_CHANCE_DAILY = "jk_draw_chance_daily:";
}
