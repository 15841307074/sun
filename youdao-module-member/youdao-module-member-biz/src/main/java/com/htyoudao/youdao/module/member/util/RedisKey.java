package com.htyoudao.youdao.module.member.util;

public interface RedisKey {
    public String AUTH_TOKEN_KEY = "auth_token_key:%s";

    public String UPDATE_VERSION_KEY = "update_version_key:%s";
    public String UPDATE_CACHE_KEY = "update_cache_key:%s";
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 微信小程序PV统计
     */
    public static final String PV_WECHAT_MINI = "pv:wechat_mini:";

    public static final String WECHAT_TOKEN = "wechat:token:";

    public static final String WECHAT_TOKEN_TIME = "wechat:token:time:";
    /**
     * 活动设置缓存key
     */
    public static final String LOTTERY_SETTING = "lottery_setting:";
    /**
     * 活动设置奖品缓存key
     */
    public static final String LOTTERY_PRIZE = "lottery_prize:";

    /**
     * 活动设置奖品抽奖次数
     */
    public static final String LOTTERY_NUM = "lottery_num:";

    /**
     * 获取抽奖商品次数
     */
    public static final String PRIZE_NUM = "prize_num:";

    /**
     * 拼单业务 长连接用户Session前缀
     */
    String SESSION_KEY_PREFIX = "splicing_session:";

    /**
     * 订单锁定标识
     */
    String SPLICING_LOCK_STATUS = "splicing_lock_status:";

    /**
     * 敏感词库
     */
    public static final String SENSITIVE= "sensitive:";
}
