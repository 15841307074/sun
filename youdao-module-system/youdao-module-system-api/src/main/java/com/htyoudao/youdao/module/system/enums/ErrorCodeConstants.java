package com.htyoudao.youdao.module.system.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 * <p>
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== AUTH 模块 1-002-000-000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1_002_000_000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1_002_000_001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1_002_000_004, "验证码不正确，原因：{}");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1_002_000_005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_MOBILE_NOT_EXISTS = new ErrorCode(1_002_000_007, "手机号未注册，请先联系管理员开通账号");
    ErrorCode AUTH_MOBILE_NOT_BIND = new ErrorCode(1_002_000_009, "手机号暂未绑定项目，请联系管理员开通");
    ErrorCode AUTH_REGISTER_CAPTCHA_CODE_ERROR = new ErrorCode(1_002_000_008, "验证码不正确，原因：{}");
    ErrorCode AUTH_BUY_NOT_PERMISSION = new ErrorCode(1_002_000_009, "暂无进货权限");
    ErrorCode AUTH_REGISTER_FORBIDDEN = new ErrorCode(1_002_000_010, "禁止注册");

    // ========== 菜单模块 1-002-001-000 ==========
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1_002_001_000, "已经存在该名字的菜单");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1_002_001_001, "父菜单不存在");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1_002_001_002, "不能设置自己为父菜单");
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1_002_001_003, "菜单不存在");
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1_002_001_004, "存在子菜单，无法删除");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1_002_001_005, "父菜单的类型必须是目录或者菜单");

    // ========== 角色模块 1-002-002-000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1_002_002_000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1_002_002_001, "已经存在名为【{}】的角色");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1_002_002_002, "已经存在标识为【{}】的角色");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1_002_002_003, "不能操作类型为系统内置的角色");
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1_002_002_004, "名字为【{}】的角色已被禁用");
    ErrorCode ROLE_ADMIN_CODE_ERROR = new ErrorCode(1_002_002_005, "标识【{}】不能使用");
    ErrorCode ROLE_USER_DUPLICATE = new ErrorCode(1_002_002_006, "用户已经拥有该角色");
    ErrorCode ROLE_MENU_NOT_EXISTS = new ErrorCode(1_002_002_007, "角色菜单不存在");
    ErrorCode ROLE_SYSTEM_CONNOT_REMOVE_USER = new ErrorCode(1_002_002_008, "项目管理员必须保证有一个用户");
    ErrorCode ROLE_USER_NOT_EMPTY = new ErrorCode(1_002_002_009, "该角色下还有用户，请先清空角色下的用户在进行删除操作");



    // ========== 用户模块 1-002-003-000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_002_003_000, "账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1_002_003_001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_002_003_002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_002_003_003, "用户不存在");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_002_003_004, "导入用户数据不能为空！");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1_002_003_005, "用户密码校验失败");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_002_003_006, "名字为【{}】的用户已被禁用");
    ErrorCode USER_COUNT_MAX = new ErrorCode(1_002_003_008, "创建用户失败，原因：超过租户最大租户配额({})！");
    ErrorCode USER_IMPORT_INIT_PASSWORD = new ErrorCode(1_002_003_009, "初始密码不能为空");
    ErrorCode USER_MOBILE_NOT_EXISTS = new ErrorCode(1_002_003_010, "该手机号尚未注册");
    ErrorCode USER_BUSINESS_USER_EXISTS = new ErrorCode(1_002_003_011, "项目已存在该账号");
    ErrorCode USER_NOT_DEACTIVATED = new ErrorCode(1_002_003_012, "没有停用用户");
    ErrorCode USER_SYSTEM_DEACTIVATED = new ErrorCode(1_002_003_013, "请确保超级管理员角色内最少存在一个启用的用户");
    ErrorCode USER_SYSTEM_DELSYSTEM = new ErrorCode(1_002_003_014, "该用户为初始项目管理员账号，无法删除，如需调整，请联系信息部");
    ErrorCode USER_GYL_EXIXTS = new ErrorCode(1_002_003_015, "供应链项目已存在该手机号");
    ErrorCode USER_OLD_PASSWORD_FAILED = new ErrorCode(1_002_003_016, "用户旧密码不正确");

    // ========== 部门模块 1-002-004-000 ==========
    ErrorCode DEPT_NAME_DUPLICATE = new ErrorCode(1_002_004_000, "已经存在该名字的部门");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1_002_004_001, "父级部门不存在");
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1_002_004_002, "当前部门不存在");
    ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1_002_004_003, "存在子部门，无法删除");
    ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1_002_004_004, "不能设置自己为父部门");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1_002_004_006, "部门({})不处于开启状态，不允许选择");
    ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1_002_004_007, "不能设置自己的子部门为父部门");
    ErrorCode DEPT_NOT_SAME_BUSINESS = new ErrorCode(1_002_004_008, "无法操作其他项目的部门");
    ErrorCode DEPT_OUT_OF_LEVEL = new ErrorCode(1_002_004_009, "部门最多五层");
    ErrorCode DEPT_USER_EXISTS = new ErrorCode(1_002_004_010, "该部门下还有用户，请先清空部门下的用户在进行删除操作");
    ErrorCode DEPT_CAN_NOT_UPDATE_BY_LEVEL = new ErrorCode(1_002_004_011, "一级部门无法修改");
    ErrorCode DEPT_NAME_EXISTS = new ErrorCode(1_002_004_011, "部门名称已存在");
    ErrorCode DEPT_CAN_NOT_ADD_USER = new ErrorCode(1_002_004_012, "最高等级部门无法添加用户");

    // ========== 岗位模块 1-002-005-000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1_002_005_000, "当前岗位不存在");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1_002_005_001, "岗位({}) 不处于开启状态，不允许选择");
    ErrorCode POST_NAME_DUPLICATE = new ErrorCode(1_002_005_002, "已经存在该名字的岗位");
    ErrorCode POST_CODE_DUPLICATE = new ErrorCode(1_002_005_003, "已经存在该标识的岗位");

    // ========== 字典类型 1-002-006-000 ==========
    ErrorCode DICT_TYPE_NOT_EXISTS = new ErrorCode(1_002_006_001, "当前字典类型不存在");
    ErrorCode DICT_TYPE_NOT_ENABLE = new ErrorCode(1_002_006_002, "字典类型不处于开启状态，不允许选择");
    ErrorCode DICT_TYPE_NAME_DUPLICATE = new ErrorCode(1_002_006_003, "已经存在该名字的字典类型");
    ErrorCode DICT_TYPE_TYPE_DUPLICATE = new ErrorCode(1_002_006_004, "已经存在该类型的字典类型");
    ErrorCode DICT_TYPE_HAS_CHILDREN = new ErrorCode(1_002_006_005, "无法删除，该字典类型还有字典数据");

    // ========== 字典数据 1-002-007-000 ==========
    ErrorCode DICT_DATA_NOT_EXISTS = new ErrorCode(1_002_007_001, "当前字典数据不存在");
    ErrorCode DICT_DATA_NOT_ENABLE = new ErrorCode(1_002_007_002, "字典数据({})不处于开启状态，不允许选择");
    ErrorCode DICT_DATA_VALUE_DUPLICATE = new ErrorCode(1_002_007_003, "已经存在该值的字典数据");

    // ========== 通知公告 1-002-008-000 ==========
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(1_002_008_001, "当前通知公告不存在");

    // ========== 短信渠道 1-002-011-000 ==========
    ErrorCode SMS_CHANNEL_NOT_EXISTS = new ErrorCode(1_002_011_000, "短信渠道不存在");
    ErrorCode SMS_CHANNEL_DISABLE = new ErrorCode(1_002_011_001, "短信渠道不处于开启状态，不允许选择");
    ErrorCode SMS_CHANNEL_HAS_CHILDREN = new ErrorCode(1_002_011_002, "无法删除，该短信渠道还有短信模板");

    // ========== 短信模板 1-002-012-000 ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_012_000, "短信模板不存在");
    ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_002_012_001, "已经存在编码为【{}】的短信模板");
    ErrorCode SMS_TEMPLATE_API_ERROR = new ErrorCode(1_002_012_002, "短信 API 模板调用失败，原因是：{}");
    ErrorCode SMS_TEMPLATE_API_AUDIT_CHECKING = new ErrorCode(1_002_012_003, "短信 API 模版无法使用，原因：审批中");
    ErrorCode SMS_TEMPLATE_API_AUDIT_FAIL = new ErrorCode(1_002_012_004, "短信 API 模版无法使用，原因：审批不通过，{}");
    ErrorCode SMS_TEMPLATE_API_NOT_FOUND = new ErrorCode(1_002_012_005, "短信 API 模版无法使用，原因：模版不存在");

    // ========== 短信发送 1-002-013-000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1_002_013_000, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_013_001, "模板参数({})缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_013_002, "短信模板不存在");

    // ========== 短信验证码 1-002-014-000 ==========
    ErrorCode SMS_CODE_NOT_FOUND = new ErrorCode(1_002_014_000, "验证码不存在");
    ErrorCode SMS_CODE_EXPIRED = new ErrorCode(1_002_014_001, "验证码过期，请重新获取");
    ErrorCode SMS_CODE_USED = new ErrorCode(1_002_014_002, "验证码已使用");
    ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1_002_014_004, "超过每日短信发送数量");
    ErrorCode SMS_CODE_SEND_TOO_FAST = new ErrorCode(1_002_014_005, "短信发送过于频繁");

    // ========== 租户信息 1-002-015-000 ==========
    ErrorCode TENANT_NOT_EXISTS = new ErrorCode(1_002_015_000, "租户不存在");
    ErrorCode TENANT_DISABLE = new ErrorCode(1_002_015_001, "名字为【{}】的租户已被禁用");
    ErrorCode TENANT_EXPIRE = new ErrorCode(1_002_015_002, "名字为【{}】的租户已过期");
    ErrorCode TENANT_CAN_NOT_UPDATE_SYSTEM = new ErrorCode(1_002_015_003, "系统租户不能进行修改、删除等操作！");
    ErrorCode TENANT_NAME_DUPLICATE = new ErrorCode(1_002_015_004, "名字为【{}】的租户已存在");
    ErrorCode TENANT_WEBSITE_DUPLICATE = new ErrorCode(1_002_015_005, "域名为【{}】的租户已存在");

    // ========== 租户套餐 1-002-016-000 ==========
    ErrorCode TENANT_PACKAGE_NOT_EXISTS = new ErrorCode(1_002_016_000, "租户套餐不存在");
    ErrorCode TENANT_PACKAGE_USED = new ErrorCode(1_002_016_001, "租户正在使用该套餐，请给租户重新设置套餐后再尝试删除");
    ErrorCode TENANT_PACKAGE_DISABLE = new ErrorCode(1_002_016_002, "名字为【{}】的租户套餐已被禁用");
    ErrorCode TENANT_PACKAGE_NAME_DUPLICATE = new ErrorCode(1_002_016_003, "已经存在该名字的租户套餐");

    // ========== 社交用户 1-002-018-000 ==========
    ErrorCode SOCIAL_USER_AUTH_FAILURE = new ErrorCode(1_002_018_000, "社交授权失败，原因是：{}");
    ErrorCode SOCIAL_USER_NOT_FOUND = new ErrorCode(1_002_018_001, "社交授权失败，找不到对应的用户");

    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_PHONE_CODE_ERROR = new ErrorCode(1_002_018_200, "获得手机号失败");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_QRCODE_ERROR = new ErrorCode(1_002_018_201, "获得小程序码失败");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_TEMPLATE_ERROR = new ErrorCode(1_002_018_202, "获得小程序订阅消息模版失败");
    ErrorCode SOCIAL_CLIENT_WEIXIN_MINI_APP_SUBSCRIBE_MESSAGE_ERROR = new ErrorCode(1_002_018_203, "发送小程序订阅消息失败");
    ErrorCode SOCIAL_CLIENT_NOT_EXISTS = new ErrorCode(1_002_018_210, "社交客户端不存在");
    ErrorCode SOCIAL_CLIENT_UNIQUE = new ErrorCode(1_002_018_211, "社交客户端已存在配置");


    // ========== OAuth2 客户端 1-002-020-000 =========
    ErrorCode OAUTH2_CLIENT_NOT_EXISTS = new ErrorCode(1_002_020_000, "OAuth2 客户端不存在");
    ErrorCode OAUTH2_CLIENT_EXISTS = new ErrorCode(1_002_020_001, "OAuth2 客户端编号已存在");
    ErrorCode OAUTH2_CLIENT_DISABLE = new ErrorCode(1_002_020_002, "OAuth2 客户端已禁用");
    ErrorCode OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS = new ErrorCode(1_002_020_003, "不支持该授权类型");
    ErrorCode OAUTH2_CLIENT_SCOPE_OVER = new ErrorCode(1_002_020_004, "授权范围过大");
    ErrorCode OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH = new ErrorCode(1_002_020_005, "无效 redirect_uri: {}");
    ErrorCode OAUTH2_CLIENT_CLIENT_SECRET_ERROR = new ErrorCode(1_002_020_006, "无效 client_secret: {}");

    // ========== OAuth2 授权 1-002-021-000 =========
    ErrorCode OAUTH2_GRANT_CLIENT_ID_MISMATCH = new ErrorCode(1_002_021_000, "client_id 不匹配");
    ErrorCode OAUTH2_GRANT_REDIRECT_URI_MISMATCH = new ErrorCode(1_002_021_001, "redirect_uri 不匹配");
    ErrorCode OAUTH2_GRANT_STATE_MISMATCH = new ErrorCode(1_002_021_002, "state 不匹配");

    // ========== OAuth2 授权 1-002-022-000 =========
    ErrorCode OAUTH2_CODE_NOT_EXISTS = new ErrorCode(1_002_022_000, "code 不存在");
    ErrorCode OAUTH2_CODE_EXPIRE = new ErrorCode(1_002_022_001, "code 已过期");

    // ========== 邮箱账号 1-002-023-000 ==========
    ErrorCode MAIL_ACCOUNT_NOT_EXISTS = new ErrorCode(1_002_023_000, "邮箱账号不存在");
    ErrorCode MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS = new ErrorCode(1_002_023_001, "无法删除，该邮箱账号还有邮件模板");

    // ========== 邮件模版 1-002-024-000 ==========
    ErrorCode MAIL_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_024_000, "邮件模版不存在");
    ErrorCode MAIL_TEMPLATE_CODE_EXISTS = new ErrorCode(1_002_024_001, "邮件模版 code({}) 已存在");

    // ========== 邮件发送 1-002-025-000 ==========
    ErrorCode MAIL_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_025_000, "模板参数({})缺失");
    ErrorCode MAIL_SEND_MAIL_NOT_EXISTS = new ErrorCode(1_002_025_001, "邮箱不存在");

    // ========== 站内信模版 1-002-026-000 ==========
    ErrorCode NOTIFY_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_026_000, "站内信模版不存在");
    ErrorCode NOTIFY_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_002_026_001, "已经存在编码为【{}】的站内信模板");

    // ========== 站内信模版 1-002-027-000 ==========

    // ========== 站内信发送 1-002-028-000 ==========
    ErrorCode NOTIFY_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_002_028_000, "模板参数({})缺失");

    // ========== 项目 1-002-029-000 ==========
    ErrorCode BUSINESS_NOT_EXISTS = new ErrorCode(1_002_029_000, "项目不存在");
    ErrorCode BUSINESS_NAME_DUPLICATE = new ErrorCode(1_002_029_001, "已经存在名为【{}】的项目");
    ErrorCode BUSINESS_CAN_NOT_UPDATE = new ErrorCode(1_002_029_002, "不能操作类型为总部类型项目");
    ErrorCode BUSINESS_NOT_VALIDITY = new ErrorCode(1_002_029_003, "项目启用时，必须处于有效期内");
    ErrorCode BUSINESS_USER_NOT_VALIDITY = new ErrorCode(1_002_029_004, "暂无项目权限，请联系管理员分配角色后再登录");
    ErrorCode BUSINESS_IS_STOP = new ErrorCode(1_002_029_004, "项目已停用，无法登陆");
    ErrorCode BUSINESS_IS_VALIDITY = new ErrorCode(1_002_029_004, "项目已到期，无法操作");
    ErrorCode BUSINESS_USER_ROLE_NOT_VALIDITY = new ErrorCode(1_002_029_004, "暂未分配角色，请联系项目管理员");
    ErrorCode BUSINESS_ID_IS_EXISTS = new ErrorCode(1_002_029_005, "项目ID已存在");

    ErrorCode BUSINESS_BOSS_USER_ROLE_NOT_FOUND = new ErrorCode(1_002_029_006, "未绑定角色，请联系运营经理");
    ErrorCode BUSINESS_BOSS_USER_MENU_NOT_FOUND = new ErrorCode(1_002_029_006, "无菜单权限，请联系运营经理");


    // ========== 组织结构 1-002-030-000 ==========
    /**
     * 组织不存在
     */
    ErrorCode ORG_NOT_EXISTS = new ErrorCode(1_002_030_000, "组织不存在");
    ErrorCode STORE_USER_NOT_EXISTS = new ErrorCode(1_002_030_001, "门店不存在");
    ErrorCode ORG_USER_NOT_EXISTS = new ErrorCode(1_002_030_002, "组织用户关系不存在");
    ErrorCode ORG_SAME_LEVEL_MOVE = new ErrorCode(1_002_030_003, "组织只能同级别移动");
    ErrorCode ORG_HAS_CHILD_ERROR = new ErrorCode(1_002_030_004, "存在下级组织,无法删除");
    ErrorCode ORG_HAS_STORE_ERROR = new ErrorCode(1_002_030_005, "该组织有门店,无法删除");
    ErrorCode ORG_HAS_USER_ERROR = new ErrorCode(1_002_030_006, "该组织有用户,无法删除");
    ErrorCode ORG_STORE_LEADER_VISIBLE = new ErrorCode(1_002_030_007, "门店店长必须可以登录");
    ErrorCode ORG_LEADER_VISIBLE = new ErrorCode(1_002_030_007, "组织负责人必须可以登录");

    // ========== 门店结构 1-002-031-000 ==========
    ErrorCode STORE_NOT_EXISTS = new ErrorCode(1_002_031_000, "门店不存在");
    ErrorCode STORE_REPEAT_EXISTS = new ErrorCode(1_002_031_001, "门店名称重复");
    ErrorCode STORE_OPEN_EXISTS = new ErrorCode(1_002_031_002, "门店正在营业无法删除");
    ErrorCode STORE_STOP_EXISTS = new ErrorCode(1_002_031_003, "门店正在营业无法关闭经营");

   ErrorCode SYSTEM_FALLBACK_ERROR = new ErrorCode(1_002_031_004, "系统繁忙，请稍后重试");

    ErrorCode SYSTEM_BLOCK_ERROR = new ErrorCode(1_002_031_005, "系统繁忙，请重试");
    ErrorCode STORE_OPEN_DELIVERY = new ErrorCode(1_002_031_006, "门店支持外卖，配送员信息和配送范围不能为空");
    ErrorCode MER_DUPLICATE_RECORD = new ErrorCode(1_002_031_007, "该门店已配置过商户号");
    ErrorCode STORE_MEITUAN_EXISTS = new ErrorCode(1_002_031_008, "美团id已配置不可重复");
    ErrorCode STORE_HUNGRY_EXISTS = new ErrorCode(1_002_031_009, "饿了么id已配置不可重复");
    ErrorCode STORE_DELIVERY_SCOPE_EMPTY = new ErrorCode(1_002_031_010, "门店支持外卖时，配送范围不能为空");
    ErrorCode STORE_DELIVERY_EXPENSE_INVALID = new ErrorCode(1_002_031_011, "门店费用配置必须为4条且费用类型0、1、2、3各一条");
    ErrorCode STORE_NEAR_NO_POSITION = new ErrorCode(1_002_031_012, "定位不能为空");
    ErrorCode STORE_CAMPUS_DELIVERY_WITHOUT_PAYMENT_CONFLICT = new ErrorCode(1_002_031_013, "不付款下单门店不支持校园配送");
    ErrorCode STORE_CAMPUS_DELIVERY_FEE_INVALID = new ErrorCode(1_002_031_014, "代取起送费不能小于配送补贴");
    ErrorCode STORE_CAMPUS_DELIVERY_EXPENSE_EMPTY = new ErrorCode(1_002_031_015, "开启校园配送时，代取起送费不能为空");
    ErrorCode STORE_CAMPUS_DELIVERY_SUBSIDY_INVALID = new ErrorCode(1_002_031_016, "配送补贴最高不能超过20元");
    ErrorCode STORE_BACKGROUND_NOT_EXISTS = new ErrorCode(1_002_031_017, "门店背景模板不存在");
    ErrorCode STORE_BACKGROUND_NAME_DUPLICATE = new ErrorCode(1_002_031_018, "门店背景模板名称已存在");
    ErrorCode STORE_BACKGROUND_PUBLISHED_NOT_EDITABLE = new ErrorCode(1_002_031_019, "已发布的门店背景模板不能编辑或删除");
    ErrorCode STORE_BACKGROUND_DEFAULT_NOT_OPERABLE = new ErrorCode(1_002_031_020, "系统默认模板不能关闭或删除");
    ErrorCode STORE_BACKGROUND_SCOPE_INVALID = new ErrorCode(1_002_031_021, "门店背景模板应用范围不合法");


    // ========== 标签 1-002-032-000 ==========
    /**
     * 标签不存在
     */
    ErrorCode TAG_VALUE_NOT_EXISTS = new ErrorCode(1_002_032_000, "标签不存在");
    ErrorCode TAG_GROUP_NOT_EXISTS = new ErrorCode(1_002_032_001, "标签组不存在");
    ErrorCode TAG_GROUP_EXISTS = new ErrorCode(1_002_032_002, "标签组已存在");
    ErrorCode TAG_VALUE_EXISTS = new ErrorCode(1_002_032_003, "标签值已存在");
    ErrorCode TAG_STORE_VALUE_EXISTS = new ErrorCode(1_002_032_004, "已有门店使用，请勿删除");
   //投诉
    ErrorCode COMPLAINT_NOT_EXISTS = new ErrorCode(1_002_033_000, "投诉内容不能为空");
    ErrorCode COMPLAINT_EXISTS = new ErrorCode(1_002_033_001, "每单只能投诉一次");
    ErrorCode COMPLAINT_CONTAINS_EMOJI_ERROR = new ErrorCode(1_002_033_002, "投诉内容不能包含表情");
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(1_002_033_003, "投诉订单不能是空");
    ErrorCode ORDER_ERR_EXISTS = new ErrorCode(1_002_033_004, "已退款/取消/未支付订单无法投诉");


    //============= 抽奖 【1_006_000_000】
    ErrorCode ORDER_NOT_SELECT_EXISTS = new ErrorCode(1_005_001_003, "没有查询到该订单");
    ErrorCode ORDER_REFUND_FORBID = new ErrorCode(1_005_001_004, "该退款订单禁止");
    ErrorCode ORDER_CANCEL_FORBID = new ErrorCode(1_005_001_005, "取消单子禁止操作");

    ErrorCode ORDER_PAYMENT_SUCCESS_FORBID = new ErrorCode(1_005_001_006, "支付成功订单禁止取消");

    ErrorCode ORDER_NOT_CROSS_STORE_WRITE_OFF = new ErrorCode(1_005_001_006, "支付成功订单禁止取消");


    //============= 小程序页面配置 【1_002_034_000】  =========
    ErrorCode APPLET_PAGE_OFF = new ErrorCode(1_002_034_001, "该页面位置不能下架,页面中至少保留一个页面位置");
    ErrorCode APPLET_PAGE_STATUS_DELETE = new ErrorCode(1_002_034_002, "当前模版为启用状态不可删除，请禁用后删除");
    ErrorCode APPLET_PAGE_DELETE = new ErrorCode(1_002_034_003, "该位置只有一个页面，不允许删除");
    ErrorCode APPLET_PAGE_PARAM = new ErrorCode(1_002_034_004, "页面参数不合法");
    ErrorCode APPLET_PAGE_TIMEOUT = new ErrorCode(1_002_034_005, "系统繁忙请重试");

    ErrorCode APPLET_PAGE_FALL_BACK = new ErrorCode(1_002_034_006, "系统繁忙请重试");

    ErrorCode APPLET_PAGE_GUARANTEE_DELETE = new ErrorCode(1_002_034_007, "当前模版不可删除");

    ErrorCode APPLET_PAGE_STATUS_GUARANTEE_DELETE = new ErrorCode(1_002_034_008, "默认模版不可修改上下架");

    ErrorCode APPLET_PAGE_NAME_UNIQUE_ERROR = new ErrorCode(1_002_034_009, "模板名称已存在");

    ErrorCode APPLET_PAGE_ONLINE_UPDATE_ERROR = new ErrorCode(1_002_034_010, "发布的模板不能编辑");

    ErrorCode APPLET_PAGE_GUARANTEE_UPDATE_ERROR = new ErrorCode(1_002_034_011, "初始模板应用范围不能修改");

    ErrorCode APPLET_PAGE_COUNT_MAX_ERROR = new ErrorCode(1_002_034_011, "模板数量超过上限");

    ErrorCode SYSTEM_PRINTER_CODE_ERROR = new ErrorCode(1_006_000_001,"打印机已注册");

    ErrorCode SYSTEM_PRINTER_INSERT_ERROR = new ErrorCode(1_006_000_002,"打印机已注册");

    ErrorCode SYSTEM_PRINTER_REGISTER_ERROR = new ErrorCode(1_006_000_003,"打印机已注册");

    ErrorCode SYSTEM_PRINTER_DELETE_FAIL = new ErrorCode(1_006_000_004,"打印机删除失败");

    ErrorCode SYSTEM_PRINTER_NAME_FAIL = new ErrorCode(1_006_000_005,"打印机名称重复");

    ErrorCode SYSTEM_PRINTER_CONTENT = new ErrorCode(1_006_000_006,"打印机内容不能为空");

    ErrorCode SYSTEM_PRINTER_CODE_D = new ErrorCode(1_006_000_007,"该打印机已在其他门店绑定");

    ErrorCode SYSTEM_PRINTER_GESHI_ERROR = new ErrorCode(1_006_000_008,"打印机格式不对");

    ErrorCode SYSTEM_INTERFACE_ERROR = new ErrorCode(1_006_000_009,"接口请求失败");

    ErrorCode SYSTEM_PRINTER_REGISTER_ERRORS = new ErrorCode(1_006_000_010,"绑定失败");
    ErrorCode SYSTEM_PRINTER_HEFA = new ErrorCode(1_006_000_011,"错误：打印机编号不合法");

    ErrorCode SYSTEM_PRINTER_REGISTER = new ErrorCode(1_006_000_012,"打印机设备编号无效");


    ErrorCode SYSTEM_JSON_ERROR = new ErrorCode(1_006_000_010,"飞鹅官网数据解析失败");

    ErrorCode PRINTER_NUMBER_INVALID = new ErrorCode(1_006_000_011,"打印机设备编号无效");

    ErrorCode PRINTER_EXIST = new ErrorCode(1_006_000_012,"打印机已存在，若当前开放平台无法查询到打印机信息，请联系售后技术支持人员核实");

    ErrorCode PRINTER_EXCEPTION = new ErrorCode(1_006_000_013,"添加打印设备失败，请稍后再试或联系售后技术支持人员");

    ErrorCode SN_OR_NAME_EMPTY = new ErrorCode(1_006_000_014,"用户添加打印机时，打印机编号或名称不能为空");

    //============= 小程序页面配置 【1_002_035_000】  =========
    ErrorCode SYS_CONFIG_KEY_NOT_EXIST = new ErrorCode(1_002_034_001, "修改参数配置%s失败，ConfigKey不存在");

    //============= 会员 【1_002_036_000】
    ErrorCode WX_MEMBER_MOBILE_ERROR = new ErrorCode(1_002_036_000, "手机号格式不正确");

    ErrorCode WX_MEMBER_MOBILE_BINDING_ERROR = new ErrorCode(1_002_036_000, "手机号换绑手机号或token非法");

    //============= 其他
    ErrorCode IOS_LINK_NOT_EXISTS = new ErrorCode(1_005_001_000, "无可用链接");


    // ========== 企微模版 1_007_000_000 ==========
    ErrorCode WX_NOT_DELETE = new ErrorCode(1_007_000_001, "默认模版不可删除");
}
