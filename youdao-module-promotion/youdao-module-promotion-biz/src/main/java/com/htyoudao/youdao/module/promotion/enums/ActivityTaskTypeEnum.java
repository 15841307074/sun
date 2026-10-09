package com.htyoudao.youdao.module.promotion.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityTaskTypeEnum {

    SIGN(1, "签到"),
    ORDER(2, "下单"),
    SHARE(3, "分享"),
    FREE(4, "免费"),
    BROWSE(5, "浏览首页");

    private final Integer code;
    private final String desc;
}
