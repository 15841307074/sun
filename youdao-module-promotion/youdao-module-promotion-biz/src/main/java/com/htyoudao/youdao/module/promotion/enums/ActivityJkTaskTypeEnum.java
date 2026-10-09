package com.htyoudao.youdao.module.promotion.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 集卡任务类型枚举
 */
@Getter
@AllArgsConstructor
public enum ActivityJkTaskTypeEnum {

    SIGN(1, "签到"),
    ORDER(2, "下单"),
    SHARE(3, "分享助力"),
    BROWSE(4, "浏览首页");

    private final Integer code;
    private final String desc;
}
