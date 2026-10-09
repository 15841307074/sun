package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomerRanking {
    CUSTOMER(0,"member_id"),
    NEW_CUSTOMER(1,"click_product"),
    REPURCHASE(2,"add_cart"),
    ;

    private final Integer code;
    private final String type;

}
