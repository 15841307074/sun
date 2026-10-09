package com.htyoudao.youdao.module.promotion.api.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * promotion 错误码枚举类
 * <p>
 * promotion 系统，使用 1-006-000-000 - 1-007-000-000段
 */
public interface ErrorCodeConstants {
    //============= 抽奖 【1_006_000_000】  =========
    ErrorCode LOTTERY_ACTIVITY_CLOSED = new ErrorCode(1_006_000_001, "该活动已关闭");
    ErrorCode LOTTERY_NO_CHANCE = new ErrorCode(1_006_000_002, "暂无抽奖机会，购买指定商品获取抽奖机会！");
    ErrorCode LOTTERY_LIMIT_REACHED = new ErrorCode(1_006_000_003, "您已到达抽奖次数上限，请明天或下单指定商品再来吧！");
    ErrorCode LOTTERY_INSUFFICIENT_POINTS = new ErrorCode(1_006_000_004, "您的积分不足，无法抽奖！");
    ErrorCode LOTTERY_LIMIT_REACHED_AGAIN = new ErrorCode(1_006_000_005, "您已到达抽奖次数上限，请明天再来吧！");
    ErrorCode LOTTERY_SYSTEM_AGAIN = new ErrorCode(1_006_000_006, "目前参与人数过多，请稍后再试！");
    ErrorCode LOTTERY_GUARANTEED_PRODUCTS = new ErrorCode(1_006_000_007, "请设置一个商品为保底商品！");
    ErrorCode LOTTERY_WINNING_PROBABILITY = new ErrorCode(1_006_000_008, "奖品列表中的中奖合计必须为100%");
    ErrorCode LOTTERY_DELETE = new ErrorCode(1_006_000_008, "该活动已启用，无法删除");

    ErrorCode LOTTERY_TITLE_HAVING = new ErrorCode(1_006_005_004, "活动标题不能重复");
    ErrorCode LOTTERY_TOT_LIMIT_REACHED_AGAIN = new ErrorCode(1_006_000_009, "您已到达抽奖次数上限，请关注下次活动吧！");
    ErrorCode LOTTERY_NOT_CROWD = new ErrorCode(1_006_000_010, "活动指定人群可参与，您暂时无法参与该活动！");
    ErrorCode LOTTERY_NOT_STORE = new ErrorCode(1_006_000_011, "活动指定门店可参与，您暂时无法参与该活动！");
    ErrorCode LOTTERY_NOT_NULL = new ErrorCode(1_006_000_012, "获取活动失败，请稍后重试！");
    ErrorCode LOTTERY_NOT_JOIN = new ErrorCode(1_006_000_013, "请先点击立即参与抽签");
    ErrorCode LOTTERY_CQ_CLOSED = new ErrorCode(1_006_000_014, "本次抽签活动已结束，感谢参与~");

    ErrorCode LOTTERY_CROWD_NOT_NULL = new ErrorCode(1_006_000_014, "自定义人群不能为空");

    ErrorCode LOTTERY_ANALYSIS_TIME_ERROR = new ErrorCode(1_006_000_015, "开始时间不能晚于结束时间");

    ErrorCode LOTTERY_ANALYSIS_TIME_OUT_ERROR = new ErrorCode(1_006_000_016, "时间间隔不能大于180天");


    ErrorCode LOTTERY_ADVERTISING_DELETE = new ErrorCode(1_006_005_015, "有广告在使用，不可删除！");
    ErrorCode LOTTERY_SESSION_CONFIG_ERROR = new ErrorCode(1_006_005_016, "场次配置错误！");
    ErrorCode LOTTERY_SESSION_NOT_IN_RANGE = new ErrorCode(1_006_005_017, "未到抽奖时间，请稍后再试");
    ErrorCode LOTTERY_SESSION_LIMIT_REACHED = new ErrorCode(1_006_005_018, "该场次次数已用完");

    ErrorCode LOTTERY_SESSION_ERROR = new ErrorCode(1_006_005_019, "抽奖场次每场至少需要间隔30分钟，请修改后再试");
    ErrorCode LOTTERY_WECOMGROUP_ERROR = new ErrorCode(1_006_005_020, "您没有加入社群，暂无抽奖资格");
    ErrorCode LOTTERY_TASK_LIMIT_REACHED = new ErrorCode(1_006_005_021, "任务已达到当前最大完成数");
    ErrorCode LOTTERY_TASK_NOT_ENABLED = new ErrorCode(1_006_005_022, "任务未开启");
    ErrorCode LOTTERY_SHARE_REPEAT = new ErrorCode(1_006_005_023, "当前时段已完成该分享任务");
    ErrorCode LOTTERY_SHARE_SELF_NOT_ALLOW = new ErrorCode(1_006_005_024, "不能给自己助力");
    ErrorCode LOTTERY_REISSUE_REJECTED = new ErrorCode(1_006_005_030, "人工补发未受理：{}");
    ErrorCode LOTTERY_BROWSE_NOT_OPEN = new ErrorCode(1_006_005_021, "当前活动未开启浏览抽奖次数");
    ErrorCode LOTTERY_BROWSE_LIMIT_REACHED = new ErrorCode(1_006_005_022, "浏览抽奖次数已达上限");
    //1_006_001_001 优惠券

    /**
     * 优惠券不存在
     */
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1_006_001_001, "优惠券不存在");
    ErrorCode COUPON_USE_TIME_ERROR = new ErrorCode(1_006_001_002, "请正确填写用券时间");
    ErrorCode COUPON_CAN_NOT_EDIT = new ErrorCode(1_006_001_003, "已经发放或者被领取的优惠券无法进行任何修改");
    ErrorCode COUPON_NUM_ERROR = new ErrorCode(1_006_001_004, "优惠券数量不能小于已领取数量");
    ErrorCode NO_THIS_STORE = new ErrorCode(1_006_001_005, "没有找到该店铺");
    ErrorCode CLAIM_COUPON_LIMITER = new ErrorCode(1_006_001_006, "前方拥堵,请稍后再试~");
    ErrorCode COUPON_EXPIRED = new ErrorCode(1_006_001_007, "优惠券链接已失效,暂时无法领取");
    ErrorCode COUPON_OVER = new ErrorCode(1_006_001_008, "优惠券已被抢光,下次记得早点来哦~");
    ErrorCode COUPON_OVER_LIMIT_0 = new ErrorCode(1_006_001_009, "领取数量已到上限");
    ErrorCode COUPON_NOT_ON_SHELF = new ErrorCode(1_006_001_010, "优惠券已下架,下次记得早点来哦~");
    ErrorCode COUPON_NOT_ON_SALE = new ErrorCode(1_006_001_011, "优惠券未上架");
    ErrorCode COUPON_NOT_REGISTER = new ErrorCode(1_006_001_012, "对不起,未注册用户无法领取优惠券");
    ErrorCode COUPON_NOT_REGISTER_NEW = new ErrorCode(1_006_001_013, "该优惠券仅限新用户可领,您暂时无法领取优惠券");
    ErrorCode COUPON_NOT_REGISTER_OLD = new ErrorCode(1_006_001_014, "该优惠券仅限老用户可领,您暂时无法领取优惠券");
    ErrorCode COUPON_NOT_REGISTER_BACK = new ErrorCode(1_006_001_015, "该优惠券仅限回归用户可领,您暂时无法领取优惠券");
    ErrorCode COUPON_NOT_CROWD = new ErrorCode(1_006_001_038, "该优惠券指定人群可领，您暂时无法领取优惠券");
    ErrorCode COUPON_NO_MOBILE = new ErrorCode(1_006_001_016, "请先绑定手机号!");
    ErrorCode COUPON_OVER_LIMIT_100 = new ErrorCode(1_006_001_017, "每天限领100张");
    ErrorCode COUPON_NO_POINT = new ErrorCode(1_006_001_018, "积分不足");
    ErrorCode COUPON_NO_REST = new ErrorCode(1_006_001_019, "优惠券【{}】剩余不足");
    ErrorCode COUPON_NO_USER = new ErrorCode(1_006_001_020, "该用户不存在,请重新选择");
    ErrorCode FILE_IS_EMPTY = new ErrorCode(1_006_001_021, "文件不存在");
    ErrorCode COUPON_NO_FILE_DATA = new ErrorCode(1_006_001_022, "请导入有效数据");
    ErrorCode COUPON_FILE_DATA_SAME = new ErrorCode(1_006_001_023, "请勿导入重复数据");
    ErrorCode COUPON_REG_ERROR = new ErrorCode(1_006_001_024, "请注册后领取");
    ErrorCode PRODUCT_IS_USED = new ErrorCode(1_006_001_025, "积分商城正在上架此优惠券，无法删除");
    ErrorCode TIME_SLOT_ERROR = new ErrorCode(1_006_001_026, "不在发放时间内");
    ErrorCode COUPON_STORE_NUM_ERROR = new ErrorCode(1_006_001_027, "此门店领取数量已到上限");
    ErrorCode COUPON_TIME_OUT = new ErrorCode(1_006_001_028, "优惠券已过期,下次记得早点来哦~");
    ErrorCode COUPON_NO_EXISTS = new ErrorCode(1_006_001_029, "优惠券不存在");
    ErrorCode WECHAT_TOKEN_ERROR = new ErrorCode(1_006_001_030, "微信token失效，有效期一小时，失效了来找我");
    ErrorCode COUPON_OVER_LIMIT = new ErrorCode(1_006_001_031, "优惠券领取次数已达上限,暂时无法领取~");
    ErrorCode PACKAGE_IS_USED = new ErrorCode(1_006_001_032, "优惠券包正在上架此优惠券，无法删除");
    ErrorCode MEMBER_NO_REGISTER = new ErrorCode(1_006_001_033, "手机号【{}】未注册");

    ErrorCode PROMOTION_FALLBACK_ERROR = new ErrorCode(1_006_001_034, "系统繁忙，请稍后重试");

    ErrorCode PROMOTION_BLOCK_ERROR = new ErrorCode(1_006_001_035, "系统繁忙，请重试");
    ErrorCode CLAIMED_CAN_NOT_DELETE = new ErrorCode(1_006_001_036, "已经领取的无法删除");
    ErrorCode CLAIM_NOT_MORE = new ErrorCode(1_006_001_037, "禁止重复提交~");

    ErrorCode SECKILL_LIMIT_MEMBER = new ErrorCode(1_006_001_038, "领券已达上限，先去使用已领的优惠券吧~");
    ErrorCode SECKILL_COUPON_GROUND = new ErrorCode(1_006_001_039, "活动商品已下架，请刷新后再试~");
    ErrorCode SECKILL_COUPON_INVENTORY = new ErrorCode(1_006_001_040, "库存不足，请重新下单~");
    ErrorCode CLAIM_FAIL = new ErrorCode(1_006_001_041, "领取失败~");
    ErrorCode FIELD_NOT_NULL = new ErrorCode(1_006_001_042, "doorsillType,discount,reduceAmount 不能为null");
    ErrorCode COUPON_COMMODITY_ERROR = new ErrorCode(1_006_001_043, "兑换券和商品券只能兑换一件商品");
    ErrorCode MEMBER_NOT_COMMUNITY = new ErrorCode(1_006_001_044, "请先加入门店社群，刷新后再试");
    ErrorCode COUPON_LIST_LIMIT = new ErrorCode(1_006_001_045, "页码限制1000以内");
    ErrorCode COUPON_EXPORT_LIMIT = new ErrorCode(1_006_001_046, "因数据体量过大，超出系统预设导出阈值暂不支持批量导出操作");


    /**
     * 1_006_002_001 优惠券包
     */

    ErrorCode COUPON_PACKAGE_NOT_EXISTS = new ErrorCode(1_006_002_001, "优惠券包不存在");
    ErrorCode COUPON_PACKAGE_NUM_NULL = new ErrorCode(1_006_002_002, "券包内至少含有有两张优惠券");
    ErrorCode COUPON_PACKAGE_NUM_SIZE = new ErrorCode(1_006_002_003, "优惠券数量不能超过10张");
    ErrorCode COUPON_PACKAGE_NUM_ERROR = new ErrorCode(1_006_002_004, "优惠券【{}】数量不足");
    ErrorCode COUPON_PACKAGE_SINGLE_NUM_LIMIT = new ErrorCode(1_006_002_005, "单个优惠券数量不能超过5张");
    ErrorCode COUPON_PACKAGE_CAN_NOT_UPDATE = new ErrorCode(1_006_002_006, "已经发放或者被领取的优惠券包无法进行任何修改");

    ErrorCode COUPON_PACKAGE_EXPIRED = new ErrorCode(1_006_002_007, "优惠券包已经过期");
    ErrorCode COUPON_PACKAGE_NOT_ON_SHELF = new ErrorCode(1_006_002_08, "优惠券已下架,下次记得早点来哦~");
    ErrorCode COUPON_PACKAGE_OVER_LIMIT = new ErrorCode(1_006_002_009, "领取数量已到上限");
    ErrorCode COUPON_PACKAGE_OVER = new ErrorCode(1_006_002_010, "优惠券已被抢光,下次记得早点来哦~");
    ErrorCode COUPON_PACKAGE_OVER_LIMIT_WEEK = new ErrorCode(1_006_002_011, "每周限领一次");
    ErrorCode COUPON_CLAIM_ERROR = new ErrorCode(1_006_002_012, "系统繁忙，请稍后重试!");
    ErrorCode COUPON_PACKAGE_USER_NOT_EXISTS = new ErrorCode(1_006_002_013, "您不是本次活动用户");
    ErrorCode COUPON_PACKAGE_NO_MOBILE = new ErrorCode(1_006_002_014, "手机号未注册");
    ErrorCode COUPON_CAN_NOT_USE = new ErrorCode(1_006_002_015, "优惠卷已过期或已下架");
    ErrorCode COUPON_PACKAGE_GROUND_ERROR_ENUM = new ErrorCode(1_006_002_016, "上架的优惠券包无法删除");
    ErrorCode PACKAGE_CLAIMED_CAN_NOT_DELETE = new ErrorCode(1_006_001_017, "已经领取的无法删除");
    ErrorCode COUPON_PACKAGE_ONE_MEMBERDAY = new ErrorCode(1_006_001_018, "会员日发放的券包只能有一个");
    ErrorCode COUPON_PACKAGE_REST_ERROR = new ErrorCode(1_006_002_019, "优惠券包数量不足");
    ErrorCode STATUS_USE_TIME_OPTION_CONFLICT = new ErrorCode(1_006_002_020, "状态与用券时间选项冲突");
    ErrorCode COUPON_PACKAGE_CLAIM_TIME_NULL = new ErrorCode(1_006_002_021, "请正确填写领取时间");





    /**
     * 广告error
     */
    ErrorCode ADVERTISEMENT_TIME_CONFLICT = new ErrorCode(1_006_002_001, "设置广告时间有冲突");
    ErrorCode ADVERTISEMENT_CATEGORY_NOT_SELECT = new ErrorCode(1_006_002_002, "广告分类未选");
    ErrorCode ADVERTISEMENT_REPEAT = new ErrorCode(1_006_002_003, "已有重复广告");

    ErrorCode ADVERTISEMENT_STORE_ID_ILLEGAL = new ErrorCode(1_006_002_04, "门店ID不合法");

    ErrorCode ADVERTISEMENT_FLOATING_ILLEGAL = new ErrorCode(1_006_002_05, "浮窗入参不合法");


    ErrorCode ADVERTISEMENT_TIME_NOT = new ErrorCode(1_006_002_06, "同时间段已存在相同位置的广告，请修改后再试");


    ErrorCode COUPON_PACKAGE_QUANTITY_ERROR_ENUM = new ErrorCode(1_006_002_011, "优惠券数量不能小于已领取数量");
    ErrorCode COUPON_PACKAGE_QUANTITY_LACK_MODIFY_ERROR_ENUM = new ErrorCode(1_006_002_012, "包内优惠券数量不足,请修改优惠券数量!");
    ErrorCode COUPON_PACKAGE_EMPTY_CANNOT_ENABLE_ERROR_ENUM = new ErrorCode(1_006_002_013, "券包中不包含优惠券，无法启用");
    ErrorCode COUPON_PACKAGE_COUPONS_EXPIRED_ERROR_ENUM = new ErrorCode(1_006_002_014, "包内优惠券已过期!");


    ErrorCode COUPON_PACKAGE_SHARE_WECHAT_JSON_PARSING_EXCEPTION_ERROR = new ErrorCode(1_006_003_001, "wechat解析json异常!");

    /**
     * 1_006_004_001 短信营销
     */
    ErrorCode PLAN_NAME_EXIST = new ErrorCode(1_006_004_001, "计划名称已存在");
    ErrorCode SEND_ERROR = new ErrorCode(1_006_004_002, "发送失败");
    ErrorCode MEMBER_NOT_ALLOWED = new ErrorCode(1_006_004_003, "您不是本次活动用户");
    ErrorCode TIME_ERROR = new ErrorCode(1_006_004_004, "该时间已存在");
    ErrorCode LAST_TIME_ERROR = new ErrorCode(1_006_004_005, "过去的时间不生效");
    ErrorCode SET_TIME_ERROR = new ErrorCode(1_006_004_006, "在8:00-10:00或14:00-16:00范围内可发送短信");

    /**
     * 1_006_005_001 njnz活动
     */
    ErrorCode NJNZ_INCOMPLETE_PARAMETERS = new ErrorCode(1_006_005_001, "几件几折参数不全");
    ErrorCode NJNZ_ISENABLED = new ErrorCode(1_006_005_002, "上架活动不允许修改");
    ErrorCode NJNZ_IS_NOT_ID = new ErrorCode(1_006_005_003, "没有id");
    ErrorCode NJNZ_NAME_HAVING = new ErrorCode(1_006_005_004, "活动名称不能重复");
    ErrorCode NJNZ_GET_ERROR = new ErrorCode(1_006_005_005, "活动获取失败");
    ErrorCode NJNZ_IS_USE = new ErrorCode(1_006_005_006, "当前活动状态无法删除");


    /**
     * 1_006_006_001  抖音相关错误码
     */
    ErrorCode DOUYIN_TOKEN_REQUEST_FAILED = new ErrorCode(1_006_006_001, "抖音Token请求失败");
    ErrorCode DOUYIN_API_REQUEST_FAILED = new ErrorCode(1_006_006_002, "抖音接口请求失败");
    ErrorCode DOUYIN_API_ERROR = new ErrorCode(1_006_006_003, "抖音接口错误");
    ErrorCode QR_CODE_INFO_ERROR = new ErrorCode(1_006_006_004, "二维码信息错误");
    ErrorCode DOUYIN_JSON_PROCESSING = new ErrorCode(1_006_006_005, "解析抖音API响应失败");
    ErrorCode DOUYIN_COUPON_REDEEM_REPEAT = new ErrorCode(1_006_006_006, "兑换码错误或已失效，请核对后再试~");
    ErrorCode DOUYIN_STORE_ID_NOT_MATCH = new ErrorCode(1_006_006_007, "当前门店暂不支持兑换该优惠券，请核对适用门店，并切换至该门店后再试~");
    ErrorCode DOUYIN_COUPON_REFUND = new ErrorCode(1_006_006_008, "抖音券码已退款，禁止兑换");
    ErrorCode DOUYIN_COUPON_PREPARE_FAILED = new ErrorCode(1_006_006_010, "抖音券验券失败");
    ErrorCode DOUYIN_COUPON_VERIFY_FAILED = new ErrorCode(1_006_006_009, "抖音券核销失败");
    ErrorCode DOUYIN_COUPON_CANCEL_FAILED = new ErrorCode(1_006_006_011, "抖音券撤销失败");
    ErrorCode DOUYIN_COUPON_REFUNDING = new ErrorCode(1_006_006_012, "抖音券正在退款中");

    ErrorCode WX_UNLIMITED_MINI_PROGRAM_CODE = new ErrorCode(1_007_001_001, "不限制的小程序码获取失败");


    /**
     * 1_006_007_001 秒杀
     */
    ErrorCode SECKILL_NOT_FOUND = new ErrorCode(1_006_005_001, "活动信息不存在");
    ErrorCode SECKILL_CACHE_NOT_FOUND = new ErrorCode(1_006_005_001, "活动已下架，下次记得早点来哦~");
    ErrorCode SECKILL_STORE_NOT_ALLOW = new ErrorCode(1_006_005_002, "当前门店未参加本次活动，请更换门店");


    /**
     * 1_006_008_001 集点
     */
    ErrorCode JD_NOT_FOUND = new ErrorCode(1_006_008_001, "活动信息不存在");
    ErrorCode JD_NOT_FOUND_V2 = new ErrorCode(1_006_008_002, "活动已结束，下次记得早点来哦~");
    ErrorCode JD_GROUND = new ErrorCode(1_006_008_003, "活动已下架，下次记得早点来哦~");
    ErrorCode JD_END = new ErrorCode(1_006_008_004, "活动已结束，下次记得早点来哦~");
    ErrorCode JD_MEMBER_COUPON_EXIST = new ErrorCode(1_006_008_005, "只能兑换一次~");
    ErrorCode JD_POINT_NOT_ENOUGH = new ErrorCode(1_006_008_006, "集点不足~");
    ErrorCode JD_GOODS_NOT_ENOUGH = new ErrorCode(1_006_008_006, " 对不起，该奖励库存不足~");



    ErrorCode JD_UPDATE_ERROR = new ErrorCode(1_006_008_002, "上架活动不允许修改");
    ErrorCode JD_INVENTORY_UPDATE_ERROR = new ErrorCode(1_006_008_002, "集点奖品库存量更新有误");

    /**
     * 1_006_009_001 满减满折
     */
    ErrorCode MJ_NAME_HAVING = new ErrorCode(1_006_009_001, "活动名称不能重复");

    ErrorCode MJ_IS_NOT_ID = new ErrorCode(1_006_009_002, "没有id");

    /**
     * 1_006_010_001 渠道名称
     */
    ErrorCode ACTIVITY_CHANNEL_NAME_DUPLICATE = new ErrorCode(1_006_010_001, "渠道名称已存在，不能重复");
    ErrorCode DEFAULT_CHANNEL_CANNOT_CHANGE_STATUS = new ErrorCode(1_006_010_002, "默认渠道不能修改状态");



    ErrorCode BASE_ACTIVITY_NOT_FOUND = new ErrorCode(1_006_011_001, "活动信息不存在");
    ErrorCode BASE_ACTIVITY_NOT_IN_GROUP = new ErrorCode(-1_006_011_002, "未进社群禁止参加活动");

    /**
     * 1_006_012_001 签到活动
     */
    ErrorCode SIGN_END_TIME_REQUIRED = new ErrorCode(1_006_012_001, "指定日期活动结束时间不能为空");
    ErrorCode SIGN_TIME_RANGE_ERROR = new ErrorCode(1_006_012_002, "活动结束时间不能早于活动开始时间");
    ErrorCode SIGN_STORE_REQUIRED = new ErrorCode(1_006_012_003, "部分门店时门店ID集合不能为空");
    ErrorCode SIGN_RESET_TYPE_REQUIRED = new ErrorCode(1_006_012_004, "开启重置时重置类型不能为空");
    ErrorCode SIGN_RESET_DAYS_REQUIRED = new ErrorCode(1_006_012_005, "开启重置时重置日期不能为空");
    ErrorCode SIGN_ACTIVITY_ID_REQUIRED = new ErrorCode(1_006_012_006, "活动ID不能为空");
    ErrorCode SIGN_EXPORT_LIMIT = new ErrorCode(1_006_012_007, "导出数据超过30万条，请联系技术手动导出");
    ErrorCode SIGN_MEMBER_MOBILE_REQUIRED = new ErrorCode(1_006_012_008, "当前会员未绑定手机号，无法参与签到活动");
    ErrorCode SIGN_DELETE_ENABLED = new ErrorCode(1_006_012_009, "活动开启状态下不允许删除");
    ErrorCode SIGN_ACTIVITY_NOT_RUNNING = new ErrorCode(1_006_012_010, "签到活动未开始或已结束");
    ErrorCode SIGN_STORE_NOT_MATCH = new ErrorCode(1_006_012_011, "当前门店暂不能参与该签到活动");
    ErrorCode SIGN_PROCESSING = new ErrorCode(1_006_012_012, "签到处理中，请勿重复点击");
    ErrorCode SIGN_TEST_DATE_FORMAT_ERROR = new ErrorCode(1_006_012_013, "签到测试日期格式错误，请使用yyyy-MM-dd");
    ErrorCode SIGN_REWARD_RECORD_NOT_EXIST = new ErrorCode(1_006_012_014, "未填写地址，无法录入快递单号");
    ErrorCode BASE_ACTIVITY_PAGE_TOO_LARGE = new ErrorCode(1_006_012_015, "查询页码不能大于1000页");
    //0707号-与运营（白晶）拉齐，已有用户参与签到活动，活动期间内不允许修改签到规则及奖励
    ErrorCode SIGN_RULE_REWARD_CANNOT_UPDATE = new ErrorCode(1_006_012_016, "已有用户参与签到活动，活动期间内不允许修改签到规则及奖励");


    /**
     * 1_006_013_001 有奖问答
     */
    ErrorCode ANSWER_ACTIVITY_ID_NOT_NULL = new ErrorCode(1_006_013_001, "有奖问答活动ID不能为空");
    ErrorCode ANSWER_ACTIVITY_NOT_FOUND = new ErrorCode(1_006_013_002, "有奖问答活动不存在");
    ErrorCode ANSWER_ACTIVITY_ENABLED_NOT_UPDATE = new ErrorCode(1_006_013_003, "活动启用中，不允许修改");
    ErrorCode ANSWER_ACTIVITY_ENABLED_NOT_DELETE = new ErrorCode(1_006_013_004, "活动启用中，不允许删除");
    ErrorCode ANSWER_CREATE_ID_NOT_ALLOWED = new ErrorCode(1_006_013_005, "新增活动不需要传ID");
    ErrorCode ANSWER_TIME_RANGE_ERROR = new ErrorCode(1_006_013_006, "活动开始时间不能晚于结束时间");
    ErrorCode ANSWER_QUESTION_NOT_EMPTY = new ErrorCode(1_006_013_007, "题目列表不能为空");
    ErrorCode ANSWER_STORE_REQUIRED_FOR_INDEPENDENT_STOCK = new ErrorCode(1_006_013_008, "门店独立库存必须选择参与门店");
    ErrorCode ANSWER_QUESTION_TYPE_ONLY_CHOICE = new ErrorCode(1_006_013_009, "现阶段题目类型只支持选择题");
    ErrorCode ANSWER_QUESTION_ONE_CORRECT = new ErrorCode(1_006_013_010, "每道题必须且只能设置一个正确答案");
    ErrorCode ANSWER_QUESTION_OPTIONS_MIN = new ErrorCode(1_006_013_011, "每道题至少需要两个选项");
    ErrorCode ANSWER_QUESTION_CORRECT_IN_OPTIONS = new ErrorCode(1_006_013_012, "正确答案必须存在于题目选项中");
    ErrorCode ANSWER_REWARD_CORRECT_COUNT_RANGE = new ErrorCode(1_006_013_013, "奖励档位答对题数不能小于0且不能大于题目总数");
    ErrorCode ANSWER_REWARD_CORRECT_COUNT_DUPLICATE = new ErrorCode(1_006_013_014, "奖励档位答对题数不能重复");
    ErrorCode ANSWER_MEMBER_MOBILE_REQUIRED = new ErrorCode(1_006_013_015, "请先授权注册");
    ErrorCode ANSWER_STORE_NOT_MATCH = new ErrorCode(1_006_013_016, "当前门店不可参与");
    ErrorCode ANSWER_TASK_NOT_ENABLED = new ErrorCode(1_006_013_017, "任务未开启");
    ErrorCode ANSWER_TASK_COUNT_NOT_CONFIGURED = new ErrorCode(1_006_013_018, "任务次数未配置");
    ErrorCode ANSWER_TASK_LIMIT_REACHED = new ErrorCode(1_006_013_019, "任务已达上限");
    ErrorCode ANSWER_SYSTEM_BUSY = new ErrorCode(1_006_013_020, "操作太频繁，请稍后再试");
    ErrorCode ANSWER_REWARD_CONFIG_ERROR = new ErrorCode(1_006_013_021, "奖品配置不正确，请联系管理员");
    ErrorCode ANSWER_REWARD_ISSUE_FAILED = new ErrorCode(1_006_013_022, "奖品发放失败，请稍后再试");
    ErrorCode ANSWER_COUPON_NOT_FOUND = new ErrorCode(1_006_013_023, "奖励优惠券不存在或已删除");
    ErrorCode ANSWER_COUPON_PACKAGE_ISSUE_FAILED = new ErrorCode(1_006_013_024, "奖励优惠券包发放失败");
    ErrorCode ANSWER_RED_PACKET_ISSUE_FAILED = new ErrorCode(1_006_013_025, "红包发放失败，请稍后再试");

    /**
     * 1_006_010_001 集卡
     */
    ErrorCode JK_GUARANTEED_CARD = new ErrorCode(1_006_010_001, "请设置一个卡片为保底卡片！");

    ErrorCode JK_TASK_LIMIT_REACHED = new ErrorCode(1_006_010_002, "任务已达到今天最大完成数");
    ErrorCode JK_TASK_NOT_ENABLED = new ErrorCode(1_006_010_003, "任务未开启");
    ErrorCode JK_HELP_REPEAT = new ErrorCode(1_006_010_004, "今天已经给该用户助力过了");
    ErrorCode JK_HELP_DAILY_LIMIT = new ErrorCode(1_006_010_005, "每人每天最多助力5次");
    ErrorCode JK_HELP_SELF_NOT_ALLOW = new ErrorCode(1_006_010_006, "不能给自己助力");
    ErrorCode JK_HELP_TARGET_LIMIT_REACHED = new ErrorCode(1_006_010_007, "该活动助力次数已达到上限");
    ErrorCode JK_NOT_ENABLED_REACHED = new ErrorCode(1_006_010_007, "暂无抽卡次数");
    ErrorCode JK_UNIVERSAL_RECORD_NOT_FOUND = new ErrorCode(1_006_010_008, "万能卡记录不存在或已使用");
    ErrorCode JK_UNIVERSAL_TARGET_INVALID = new ErrorCode(1_006_010_009, "万能卡只能兑换套系卡");
    ErrorCode JK_PRIZE_NOT_FOUND = new ErrorCode(1_006_010_010, "奖品不存在");
    ErrorCode JK_PRIZE_CARD_INVALID = new ErrorCode(1_006_010_011, "当前卡片不满足该奖品兑换条件");
    ErrorCode JK_PRIZE_CARD_LIMIT = new ErrorCode(1_006_010_011, "兑换已达到上限");
    ErrorCode JK_PRIZE_STOCK_NOT_ENOUGH = new ErrorCode(1_006_010_012, "奖品库存不足");
    ErrorCode JK_EXCHANGE_RECORD_NOT_FOUND = new ErrorCode(1_006_010_013, "兑换记录不存在");

    ErrorCode JK_ALL_CARD = new ErrorCode(1_006_010_014, "万能卡只能设置一张！");

    ErrorCode JK_HIDE_CARD = new ErrorCode(1_006_010_015, "隐藏卡只能设置一张！");

    /** 1_006_012_001 问卷调查 */
    ErrorCode SURVEY_NOT_EXISTS = new ErrorCode(1_006_012_001, "问卷不存在");
    ErrorCode SURVEY_NOT_ALLOW_EDIT = new ErrorCode(1_006_012_002, "仅未发布状态可编辑");
    ErrorCode SURVEY_NOT_ALLOW_DELETE = new ErrorCode(1_006_012_003, "进行中的问卷不可删除");
    ErrorCode SURVEY_NOT_ALLOW_PUBLISH = new ErrorCode(1_006_012_004, "仅未发布状态可发布");
    ErrorCode SURVEY_NOT_ALLOW_STOP = new ErrorCode(1_006_012_005, "仅进行中状态可停用");
    ErrorCode SURVEY_QUESTION_NOT_ENOUGH = new ErrorCode(1_006_012_006, "问卷至少需要2道题目");
    ErrorCode SURVEY_NOT_IN_PROGRESS = new ErrorCode(1_006_012_007, "问卷未在进行中");
    ErrorCode SURVEY_ALREADY_SUBMITTED = new ErrorCode(1_006_012_008, "您已提交过问卷");
    ErrorCode SURVEY_SUBMIT_LIMIT_REACHED = new ErrorCode(1_006_012_009, "提交次数已达上限");
    ErrorCode SURVEY_REQUIRED_NOT_ANSWERED = new ErrorCode(1_006_012_010, "请完成所有必答题");

    ErrorCode SURVEY_WECOMGROUP_ERROR = new ErrorCode(1_006_012_011, "您没有加入社群，暂无填写问卷资格");

    /** 1_006_013_001 投票活动 */
    ErrorCode VOTE_NOT_EXISTS = new ErrorCode(1_006_013_001, "投票活动不存在");
    ErrorCode VOTE_IS_ENABLED = new ErrorCode(1_006_013_002, "上架活动不允许修改");
    ErrorCode VOTE_IS_USE = new ErrorCode(1_006_013_003, "当前活动状态无法删除");
    ErrorCode VOTE_BEFORE_PROGRESS = new ErrorCode(1_006_013_004, "活动即将开始，敬请期待~");
    ErrorCode VOTE_AFTER_PROGRESS = new ErrorCode(1_006_013_014, "活动已结束，下次早点来哦~");
    ErrorCode VOTE_NOT_IN_PROGRESS = new ErrorCode(1_006_013_015, "活动暂未开启");
    ErrorCode VOTE_COUNT_LIMIT = new ErrorCode(1_006_013_005, "投票次数已达上限");
    ErrorCode VOTE_OPTION_NOT_EXISTS = new ErrorCode(1_006_013_006, "投票选项不存在");
    ErrorCode VOTE_REWARD_STOCK_NOT_ENOUGH = new ErrorCode(1_006_013_007, "奖品库存不足");
    ErrorCode VOTE_WECOMGROUP_ERROR = new ErrorCode(1_006_013_008, "您没有加入社群，暂无投票资格");
    ErrorCode VOTE_IS_NOT_ID = new ErrorCode(1_006_013_009, "没有id");
    ErrorCode VOTE_NOT_STORE = new ErrorCode(1_006_013_010, "当前门店暂无投票资格");
    ErrorCode VOTE_COUNT_OVER_LIMIT = new ErrorCode(1_006_013_011, "投票次数不能超过9999");
    ErrorCode VOTE_TIME_ERROR = new ErrorCode(1_006_013_012, "活动开始时间不能晚于结束时间");
    ErrorCode VOTE_PRIZE_TYPE_NOT_EXISTS = new ErrorCode(1_006_013_013, "奖励的奖品类型不能为空");
    /**
     * 1_006_014_001 满赠活动
     */
    ErrorCode MZ_NAME_HAVING = new ErrorCode(1_006_014_001, "活动名称不能重复");
    ErrorCode MZ_IS_NOT_ID = new ErrorCode(1_006_014_002, "没有id");
    ErrorCode MZ_GIFT_THRESHOLD_DUPLICATE = new ErrorCode(1_006_014_003, "赠送门槛金额重复，请修改后再试");
    ErrorCode MZ_GIFT_NOT_SELECTED = new ErrorCode(1_006_014_004, "请选择赠送商品");
    ErrorCode MZ_THRESHOLD_EXCEED_LIMIT = new ErrorCode(1_006_014_005, "优惠门槛金额不能超过99999");
    ErrorCode MZ_INVENTORY_EXCEED_LIMIT = new ErrorCode(1_006_014_006, "活动库存不能超过1000000");
    ErrorCode MZ_CIRCULAR_ONLY_ONE_GIFT = new ErrorCode(1_006_014_007, "循环满赠只允许添加1个赠品");
    ErrorCode MZ_PRODUCT_MULTI_SPEC_ERROR = new ErrorCode(1_006_014_008, "活动商品不支持多规格商品，请修改后再试");
}
