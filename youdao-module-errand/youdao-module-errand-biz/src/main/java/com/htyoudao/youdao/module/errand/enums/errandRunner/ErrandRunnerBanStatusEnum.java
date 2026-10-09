package com.htyoudao.youdao.module.errand.enums.errandRunner;

import lombok.Getter;

/**
 * 跑腿员封禁状态枚举。
 */
@Getter
public enum ErrandRunnerBanStatusEnum {

    NORMAL(0, "正常"),
    BANNED(1, "封禁");

    private final int code;
    private final String name;

    ErrandRunnerBanStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
