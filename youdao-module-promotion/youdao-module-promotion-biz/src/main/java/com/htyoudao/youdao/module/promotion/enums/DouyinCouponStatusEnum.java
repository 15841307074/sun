package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

/**
 * @description:
 * @program: cloud-0090
 * @author: Mr.JHzhang
 * @create: 2025-07-09 13:24
 **/
@Getter
public enum DouyinCouponStatusEnum {

    INIT(0, "初始状态"),
    AVAILABLE(1, "未使用"),
    USED(2, "已使用"),
    REFUND_APPLYING(3, "退款申请中(待审核)"),
    REFUND_SUCCESS(4, "退款成功"),
    REFUND_FAILED(5, "退款失败"),
    REFUNDING(6, "退款中"),
    USING(10, "使用中（周期/储值卡激活）");

    private final int code;
    private final String description;

    DouyinCouponStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
