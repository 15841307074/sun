package com.htyoudao.youdao.module.order.service.activity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountRules {

    STEP(1), //阶梯优惠
    LOOP(2),; //循环优惠

    final Integer type;
}
