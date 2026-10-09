package com.htyoudao.youdao.module.order.core.calc.DTO;

import com.htyoudao.youdao.module.order.enums.NjnzDiscountTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDiscountDTO {
    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
     */
    private Integer discountType;

    /**
     * 优惠折扣（1满xx元减xx元 2满xx元减xx折）
     */
    private Integer discountOffer;

    /**
     * 活动名称
     */
    private String activityName;

    /** 前端展示标签；N件N折会根据优惠类型及规则生成具体文案。 */
    private String activityTag;

    /**
     * 优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    public static String buildActivityTag(Integer activityType, Integer discountType,
                                          Integer discountItemNum, Double discountRate,
                                          String originalActivityTag) {
        if (Integer.valueOf(ActivityTypeEnum.MZ.getCode()).equals(activityType)) {
            return "赠品优惠";
        }
        // 满减满折的原始标签已包含具体档位，例如“满10元减2元”“满3件打8折”。
        if (Integer.valueOf(ActivityTypeEnum.MJ.getCode()).equals(activityType)) {
            return originalActivityTag;
        }
        if (!Integer.valueOf(ActivityTypeEnum.NJ_NZ.getCode()).equals(activityType) || discountType == null) {
            return originalActivityTag;
        }
        if (discountType == NjnzDiscountTypeEnum.SECOND_HALF_PRICE.getCode()) {
            return NjnzDiscountTypeEnum.SECOND_HALF_PRICE.getDescription();
        }
        if (discountType == NjnzDiscountTypeEnum.BUY_ONE_GET_ONE.getCode()) {
            return NjnzDiscountTypeEnum.BUY_ONE_GET_ONE.getDescription();
        }
        if (discountType == NjnzDiscountTypeEnum.CUSTOM.getCode()) {
            if (discountItemNum == null || discountRate == null) {
                return NjnzDiscountTypeEnum.CUSTOM.getDescription();
            }
            String rate = BigDecimal.valueOf(discountRate).stripTrailingZeros().toPlainString();
            return "第" + discountItemNum + "件" + rate + "折";
        }
        return originalActivityTag;
    }
}
