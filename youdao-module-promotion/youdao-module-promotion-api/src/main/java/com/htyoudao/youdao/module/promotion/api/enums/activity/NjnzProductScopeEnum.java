package com.htyoudao.youdao.module.promotion.api.enums.activity;

public enum NjnzProductScopeEnum {

    ALL(1, "全部商品适用"),
    SPECIFIC(2, "指定商品适用");

    private final int code;
    private final String description;

    NjnzProductScopeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NjnzProductScopeEnum of(int code) {
        for (NjnzProductScopeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new IllegalArgumentException("未知的活动适用类型: " + code);
    }
}