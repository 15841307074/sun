package com.htyoudao.youdao.module.promotion.api.enums.coupon;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:  优惠券门店标签sql类型
 * @author dht
 */
@Getter
@AllArgsConstructor
public enum CouponStoreTagSqlTypeEnum {

    /**
     * 删除操作
     */
    DELETE,

    /**
     * 更新/保存操作
     */
    UPDATE;
}
