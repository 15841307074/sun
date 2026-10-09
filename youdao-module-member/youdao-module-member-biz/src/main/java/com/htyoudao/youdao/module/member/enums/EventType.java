package com.htyoudao.youdao.module.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    IN_STORE("进店","in_store"),
    CLICK_PRODUCT("点击商品","click_product"),
    ADD_CART("加入购物车","add_cart"),
    SHARE("分享","share"),
    ;
    private final String name;
    private final String code;
}
