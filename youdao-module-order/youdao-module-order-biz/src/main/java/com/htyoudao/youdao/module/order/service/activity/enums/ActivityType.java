package com.htyoudao.youdao.module.order.service.activity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityType {
    NJ_NZ(1),// N件N折

    MJ_MZ(4),// 满减满折

    ;
    final Integer type;

}