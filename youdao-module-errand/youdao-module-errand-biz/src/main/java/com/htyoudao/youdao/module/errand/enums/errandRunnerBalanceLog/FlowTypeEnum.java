package com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流水类型枚举
 */
/**
 * 流水类型枚举
 */
@Getter
@AllArgsConstructor
public enum FlowTypeEnum {

    INCOME(1, "赏金入账"),
    WITHDRAW(2, "提现扣减"),
    REFUND_DEDUCT(3, "退款扣回"),
    WITHDRAW_FAIL_RETURN(4, "提现失败退回"),
    MANUAL_ADJUST(5, "人工调整"),
    REWARD_UNFREEZE(6, "赏金解冻");

    private final Integer code;
    private final String description;

    public static FlowTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FlowTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        FlowTypeEnum type = getByCode(code);
        return type != null ? type.getDescription() : "";
    }
}
