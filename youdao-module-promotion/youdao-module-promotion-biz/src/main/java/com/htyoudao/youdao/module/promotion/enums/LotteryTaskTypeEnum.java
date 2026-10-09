package com.htyoudao.youdao.module.promotion.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LotteryTaskTypeEnum {

    FREE(1, "免费"),
    POINTS(2, "积分"),
    ORDER(3, "下单"),
    SHARE(4, "分享"),
    BROWSE(5, "浏览首页");

    private final Integer code;
    private final String desc;
}
