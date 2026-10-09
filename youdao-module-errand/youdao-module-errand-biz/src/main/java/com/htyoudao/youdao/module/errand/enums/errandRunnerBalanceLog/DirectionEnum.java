package com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 方向枚举
 */
@Getter
@AllArgsConstructor
public enum DirectionEnum {

    INCOME(1, "收入"),
    EXPENDITURE(2, "支出");

    private final Integer code;
    private final String description;

    public static DirectionEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DirectionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        DirectionEnum direction = getByCode(code);
        return direction != null ? direction.getDescription() : "";
    }
}
