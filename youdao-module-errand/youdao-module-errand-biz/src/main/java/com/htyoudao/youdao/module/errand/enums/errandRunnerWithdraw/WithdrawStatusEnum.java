package com.htyoudao.youdao.module.errand.enums.errandRunnerWithdraw;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现状态枚举
 */
@Getter
@AllArgsConstructor
public enum WithdrawStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "提现中"),
    SUCCESS(2, "已提现"),
    FAIL(3, "提现失败");

    private final Integer code;
    private final String description;

    public static WithdrawStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WithdrawStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code){
        WithdrawStatusEnum withdrawStatusEnum = getByCode(code);
        return withdrawStatusEnum == null ? null : withdrawStatusEnum.getDescription();
    }


    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }

    public static boolean isFail(Integer code) {
        return FAIL.getCode().equals(code);
    }

    public static boolean isPending(Integer code) {
        return PENDING.getCode().equals(code);
    }
}
