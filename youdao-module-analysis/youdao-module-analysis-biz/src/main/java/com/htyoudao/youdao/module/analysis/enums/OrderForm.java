package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// OrderForm 枚举（订单来源）
@Getter
@AllArgsConstructor
public enum OrderForm {
    DC(0), WECHAT(1), ALIPAY(2);
    private final Integer form;
}