
package com.htyoudao.youdao.module.order.service.activity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DiscountOffer {

    JY(1), //1满xx元减xx元
    JZ(2),; //2满xx元减xx折

    final Integer type;

    public static DiscountOffer getByType(Integer type) {
        for (DiscountOffer discountOffer : DiscountOffer.values()) {
            if (discountOffer.getType().equals(type)) {
                return discountOffer;
            }
        }
        return null;
    }
}
