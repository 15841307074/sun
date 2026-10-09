package com.htyoudao.youdao.module.promotion.api.activity.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityMJDTO extends ActivityBaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4150891991743481771L;


    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 优惠类型（优惠类型 （1满N元，2满N件））
     */
    @Schema(description = "优惠类型（优惠类型 （1满N元，2满N件））")
    private Integer discountType;

    /**
     * 标签名
     */
    @Schema(description = "标签名")
    private String tag;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;


    /**
     * 优惠折扣（1满xx元减xx元 2满xx元减xx折）
     */
    @Schema(description = "优惠折扣（1满xx元/件减xx元 2满xx元/件减xx折）")
    private Integer discountOffer;

    /**
     * 优惠设置
     */
    @Schema(description = "优惠设置")
    private String discountSettings;

    /**
     * 优惠设置
     */
    @Schema(description = "优惠设置拆解集合")
    private List<DiscountSettingsDTO> discountSettingsList;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    private Integer discountRules;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
     */
    @Schema(description = "可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）")
    private List<Integer> stackableActivities;

    /**
     * 活动备注
     */
    private String activityRemark;

    private LocalDateTime createTime;
}
