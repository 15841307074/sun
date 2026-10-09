package com.htyoudao.youdao.module.errand.api.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * 跑腿员错误码常量。
 *
 * <p>跑腿员模块使用 1_006_000_000 段。</p>
 */
public interface ErrorCodeConstants {

    // ========== 跑腿员基础模块 1_006_001_000 段 ==========
    ErrorCode ERRAND_RUNNER_NOT_EXISTS = new ErrorCode(1_006_001_001, "跑腿员不存在");
    ErrorCode ERRAND_RUNNER_MOBILE_NOT_EXISTS = new ErrorCode(1_006_001_002, "该手机号未注册跑腿员");
    ErrorCode ERRAND_RUNNER_ALREADY_EXISTS = new ErrorCode(1_006_001_003, "跑腿员已存在");
    ErrorCode ERRAND_RUNNER_AUDIT_FAIL = new ErrorCode(1_006_001_004, "审核失败");
    ErrorCode ERRAND_RUNNER_BANNED = new ErrorCode(-1_006_001_005, "跑腿员已被封禁");
    ErrorCode ERRAND_RUNNER_LOGIN_USER_EMPTY = new ErrorCode(1_006_001_006, "请先登录后再申请骑手进驻");
    ErrorCode ERRAND_RUNNER_APPLY_NOT_EXISTS = new ErrorCode(1_006_001_007, "骑手进驻申请不存在");
    ErrorCode ERRAND_RUNNER_APPLY_PENDING = new ErrorCode(1_006_001_008, "骑手进驻申请审核中，请勿重复提交");
    ErrorCode ERRAND_RUNNER_APPLY_APPROVED = new ErrorCode(1_006_001_009, "骑手进驻申请已通过，不可修改");
    ErrorCode ERRAND_RUNNER_PHONE_DUPLICATE = new ErrorCode(1_006_001_010, "该手机号已提交骑手进驻申请");
    ErrorCode ERRAND_RUNNER_IMAGE_EMPTY = new ErrorCode(1_006_001_011, "请上传身份证正反面和学生证照片");
    ErrorCode ERRAND_RUNNER_FIRST_AUDIT_NOT_PASSED = new ErrorCode(1_006_001_012, "成为骑手后可查看详情");
    ErrorCode ERRAND_RUNNER_APPLY_AUDITING_NOT_ALLOW_UPDATE = new ErrorCode(1_006_001_013, "骑手入驻申请审核中，不可修改");
    ErrorCode ERRAND_RUNNER_APPLY_STATUS_NOT_ALLOW_UPDATE = new ErrorCode(1_006_001_014, "骑手入驻申请状态不允许修改");
    ErrorCode ERRAND_RUNNER_APPLY_NO_CHANGE = new ErrorCode(1_006_001_015, "申请资料未发生变化，无需修改");
    ErrorCode ERRAND_REWARD_AMOUNT_INVALID = new ErrorCode(1_006_001_016, "跑腿赏金金额无效");
    ErrorCode ERRAND_REWARD_INCOME_FAIL = new ErrorCode(1_006_001_017, "跑腿赏金入账失败");
    ErrorCode ERRAND_RUNNER_ID_CARD_NO_DUPLICATE = new ErrorCode(1_006_001_018, "该身份证号已提交骑手入驻申请");

    // ========== 交易密码模块 1_006_002_000 段 ==========
    ErrorCode TRAN_PASSWORD_NOT_SET = new ErrorCode(1_006_002_001, "请先设置交易密码");
    ErrorCode TRAN_PASSWORD_ALREADY_SET = new ErrorCode(1_006_002_002, "交易密码已设置，请勿重复设置");
    ErrorCode TRAN_PASSWORD_NOT_MATCH = new ErrorCode(1_006_002_003, "两次输入的密码不一致");
    ErrorCode TRAN_PASSWORD_FORMAT_ERROR = new ErrorCode(1_006_002_004, "交易密码必须为6位数字");
    ErrorCode TRAN_PASSWORD_ERROR = new ErrorCode(1_006_002_005, "交易密码错误");
    ErrorCode TRAN_PASSWORD_SET_FAIL = new ErrorCode(1_006_002_006, "设置交易密码失败");
    ErrorCode TRAN_PASSWORD_VERIFY_FAIL = new ErrorCode(1_006_002_007, "交易密码验证失败");

    ErrorCode TRAN_PASSWORD_RUNNER_ONLY = new ErrorCode(1_006_002_009, "只有跑腿员才能操作");

    ErrorCode TRAN_PASSWORD_LOCKED = new ErrorCode(1_006_002_008, "交易密码输入错误次数过多，请%s分钟后重试");

    // ========== 登录密码模块 1_006_003_000 段 ==========
    ErrorCode PASSWORD_NOT_MATCH = new ErrorCode(1_006_003_001, "两次输入的密码不一致");
    ErrorCode PASSWORD_FORMAT_ERROR = new ErrorCode(1_006_003_002, "密码必须包含字母和数字，长度6-20位");
    ErrorCode OLD_PASSWORD_ERROR = new ErrorCode(1_006_003_003, "原密码错误");
    ErrorCode UPDATE_PASSWORD_FAIL = new ErrorCode(1_006_003_004, "修改密码失败");
    ErrorCode FORGET_PASSWORD_FAIL = new ErrorCode(1_006_003_005, "找回密码失败");

    // ========== 短信验证码模块 1_006_004_000 段 ==========
    ErrorCode SMS_CODE_ERROR = new ErrorCode(1_006_004_001, "验证码错误或已过期");
    ErrorCode SMS_CODE_SEND_FAIL = new ErrorCode(1_006_004_002, "验证码发送失败");

    // ========== 提现模块 1_006_005_000 段 ==========
    ErrorCode WITHDRAW_AMOUNT_INVALID = new ErrorCode(1_006_005_001, "提现金额无效");
    ErrorCode WITHDRAW_BALANCE_NOT_ENOUGH = new ErrorCode(1_006_005_002, "余额不足");
    ErrorCode WITHDRAW_FAIL = new ErrorCode(1_006_005_003, "提现失败");
    ErrorCode WITHDRAW_AMOUNT_TOO_SMALL = new ErrorCode(1_006_005_004, "提现金额不能小于%s元");
    ErrorCode WITHDRAW_AMOUNT_TOO_LARGE = new ErrorCode(1_006_005_005, "提现金额不能大于%s元");
    ErrorCode WITHDRAW_DAILY_LIMIT = new ErrorCode(1_006_005_006, "今日提现次数已达上限");
    ErrorCode WITHDRAW_EXPORT_TOO_MANY = new ErrorCode(1_006_005_007, "导出上限为30万条数据，请缩小查询范围");

    // ========== 审核模块 1_006_006_000 段 ==========
    ErrorCode ERRAND_RUNNER_AUDIT_PENDING = new ErrorCode(1_006_006_001, "跑腿员正在审核中");
    ErrorCode ERRAND_RUNNER_AUDIT_ALREADY_PASSED = new ErrorCode(1_006_006_002, "跑腿员已审核通过");
    ErrorCode ERRAND_RUNNER_AUDIT_ALREADY_FAILED = new ErrorCode(1_006_006_003, "跑腿员已审核失败");
    ErrorCode ERRAND_RUNNER_AUDIT_FAIL_REASON_REQUIRED = new ErrorCode(1_006_006_004, "审核失败原因不能为空");
    ErrorCode ERRAND_RUNNER_AUDIT_PASS_FAIL = new ErrorCode(1_006_006_005, "审核通过失败");
    ErrorCode ERRAND_RUNNER_AUDIT_FAIL_ERROR = new ErrorCode(1_006_006_006, "审核失败操作失败");

    // ========== 封禁模块 1_006_007_000 段 ==========
    ErrorCode ERRAND_RUNNER_BAN_ALREADY = new ErrorCode(1_006_007_001, "跑腿员已被封禁");
    ErrorCode ERRAND_RUNNER_UNBAN_ALREADY = new ErrorCode(1_006_007_002, "跑腿员已是正常状态");
    ErrorCode ERRAND_RUNNER_BAN_REASON_REQUIRED = new ErrorCode(1_006_007_003, "封禁原因不能为空");
    ErrorCode ERRAND_RUNNER_BAN_FAIL = new ErrorCode(1_006_007_004, "封禁操作失败");
    ErrorCode ERRAND_RUNNER_UNBAN_FAIL = new ErrorCode(1_006_007_005, "解封操作失败");
    ErrorCode ERRAND_RUNNER_HAS_UNFINISHED_ORDERS = new ErrorCode(1_006_007_006, "当前跑腿员有未完结订单，暂时无法封禁");
}
