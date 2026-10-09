package com.htyoudao.youdao.module.order.service.activity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountType {

    MY(1), //满元
    MJ(2),; //满件

    final Integer type;


    public static DiscountType getByType(Integer type) {
        for (DiscountType discountType : DiscountType.values()) {
            if (discountType.getType().equals(type)) {
                return discountType;
            }
        }
        return null;
    }


}
