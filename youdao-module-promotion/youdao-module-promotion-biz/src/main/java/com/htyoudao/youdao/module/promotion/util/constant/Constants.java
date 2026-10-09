package com.htyoudao.youdao.module.promotion.util.constant;


public interface Constants {
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 所有权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 角色权限分隔符
     */
    public static final String ROLE_DELIMETER = ",";

    /**
     * 权限标识分隔符
     */
    public static final String PERMISSION_DELIMETER = ",";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 用户头像
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    public static final String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = {"org.springframework", "com.htyd"};

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     */
    public static final String[] JOB_WHITELIST_STR = {"com.htyd"};

    /**
     * 定时任务违规的字符
     */
    public static final String[] JOB_ERROR_STR = {"java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache"};


    public static final String STRING_ZERO = "0";

    public static final String STRING_ONE = "1";

    public static final int INT_ZERO = 0;

    public static final int INT_ONE = 1;

    public static final int INT_TWO = 2;

    public static final int INT_THREE = 3;

    public static final int INT_FOUR = 4;

    public static final int INT_FIVE = 5;

    public static final String SIGN_GT = ">";

    public static final String SIGN_LT = "<";

    public static final String SIGN_EQ = "=";

    public static final String SIGN_ADD = "+";

    public static final String SIGN_SUB = "-";

    public static final String SYSTEM_NAME = "系统";

    /**
     * 出库
     */
    public static final String CK_NUMBER_FIX = "CK";

    /**
     * 入库
     */
    public static final String RK_NUMBER_FIX = "RK";

    /**
     * 订货
     */
    public static final String DH_NUMBER_FIX = "DH";

    /**
     * 退货
     */
    public static final String TH_NUMBER_FIX = "TH";

    /**
     * 预存款
     */
    public static final String YC_NUMBER_FIX = "YC";

    /**
     * 充值预存款
     */
    public static final String CZ_NUMBER_FIX = "CZ";

    /**
     * 支付
     */
    public static final String PAY_NUMBER_FIX = "PY";

    /**
     * 订单超时时间
     */
    public static final long ORDER_TIME_OUT_MINUTES = 10;

    /**
     * 预存款订单超时时间
     */
    public static final long RECHARGE_TIME_OUT_MINUTES = 15;

    /**
     * 每页最大数量
     */
    public static final long PAGE_SIZE_MAX = 500000;

    /**
     * 微信小程序PV统计
     */
    public static final String PV_HBGC = "pv:hbgc:";

    /**
     * 拼单
     */
    public static final String SPLICING_ORDER_KEY = "splicing_order:";

    /**
     * 拼单lock
     */
    public static final String SPLICING_ORDER_LOCK_KEY = "splicing_order_lock:";
}
