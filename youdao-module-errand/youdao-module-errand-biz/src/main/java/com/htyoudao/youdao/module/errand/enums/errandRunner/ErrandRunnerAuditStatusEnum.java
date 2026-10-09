package com.htyoudao.youdao.module.errand.enums.errandRunner;

import lombok.Getter;

/**
 * 跑腿员审核状态枚举。
 */
@Getter
public enum ErrandRunnerAuditStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "失败");

    private final int code;
    private final String name;

    ErrandRunnerAuditStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
