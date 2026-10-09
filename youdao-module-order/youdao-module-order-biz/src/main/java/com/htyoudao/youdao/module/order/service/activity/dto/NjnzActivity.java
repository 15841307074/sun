package com.htyoudao.youdao.module.order.service.activity.dto;

import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 第n件打n折活动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NjnzActivity implements Activity {


    private Long createTime;

    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 是否优惠叠加（0-否，1-是）
     */
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
     */
    private List<Integer> stackableActivities;

    /**
     * 活动备注（最多200字）
     */
    private String activityRemark;

    /**
     * 优惠第几件
     */
    private Integer discountItemNum;

    /**
     * 优惠打几折
     */
    private Double discountRate;


    @Override
    public Double calculateDiscount(List<ProductItem> items) {
        Double discountAmount = getDiscountAmountByRate(items.get(0).getPrice(), discountRate);
        return discountAmount * getDiscountQuantity(items);
    }

    @Override
    public Boolean canApply(List<ProductItem> items) {
        return getTotalCount(items) >= discountItemNum;
    }

    @Override
    public Integer getDiscountQuantity(List<ProductItem> items) {
        return getTotalCount(items) / discountItemNum;
    }
}
