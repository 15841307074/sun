package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySeckillCouponRespVO {

    // 主键
    private Long id;

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    private Long couponId;

    /**
     * 显示价格
     */
    @Schema(description = "显示价格")
    private BigDecimal showPrice;

    /**
     * 显示标题
     */
    @Schema(description = "显示标题")
    private String showTitle;

    /**
     * 活动库存
     */
    @Schema(description = "活动库存")
    private Integer activityStock;

    /**
     * 每项限制数量
     */
    @Schema(description = "每项限制数量")
    private Integer limitPerItem;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券明细")
    private GoodCouponDO coupon;

}
