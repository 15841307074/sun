package com.htyoudao.youdao.module.promotion.api.activity.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityMzDTO extends ActivityBaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3915086221404665193L;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 活动主表id（activity_mz 关联的活动id）
     */
    @Schema(description = "活动主表id")
    private Long activityId;

    /**
     * 优惠类型（1满N元赠商品，2满N件赠商品）
     */
    @Schema(description = "优惠类型（1满N元赠商品，2满N件赠商品）")
    private Integer discountType;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    private Integer discountRules;

    /**
     * 下单类型（1按品类限制，2按商品限制）
     */
    @Schema(description = "下单类型（1按品类限制，2按商品限制）")
    private Integer placeOrderType;

    /**
     * 参与品类（1全部 2仅单品 3仅套餐，null按全部处理）
     */
    @Schema(description = "参与品类（1全部 2仅单品 3仅套餐，null按全部处理）")
    private Integer categoryType;

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
     * 赠品列表
     */
    @Schema(description = "赠品列表")
    private List<MzGiftDTO> gifts;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
     */
    @Schema(description = "可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）")
    private List<Integer> stackableActivities;

    /**
     * 用户参与限制（0不限制，1每人每天，2每人最多）
     */
    @Schema(description = "用户参与限制（0不限制，1每人每天，2每人最多）")
    private Integer userLimitType;

    /**
     * 用户参与限制次数
     */
    @Schema(description = "用户参与限制次数")
    private Integer userLimitValue;

    /**
     * 活动备注
     */
    private String activityRemark;

    private LocalDateTime createTime;
}
