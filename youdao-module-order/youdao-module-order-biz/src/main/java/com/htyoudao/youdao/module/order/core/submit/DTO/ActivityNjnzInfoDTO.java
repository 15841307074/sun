package com.htyoudao.youdao.module.order.core.submit.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActivityNjnzInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6583220401131368377L;
    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
     */
    private Integer discountType;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 优惠第几件
     */
    private Integer discountItemNum;

    /**
     * 优惠打几折
     */
    private double discountRate;

    /**
     * 满减、满折
     */
    private Integer discountOffer;

    /**
     * 优惠叠加（0不叠加 1叠加）
     */
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    private Integer couponStackable;

    /**
     * 标签名
     */
    private String tag;

    /**
     * 优惠标签
     */
    private String activityTag;

    private String stackableActivities;
}
