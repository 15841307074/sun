package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领取优惠券参数
 * @author dht
 */
@Schema(description = "app - 领取优惠券参数 Request VO")
@Data
public class ClaimCouponReqVO {

    @Schema(description = "memberId", example = "赵六")
    @NotNull(message = "memberId不能为空")
    private Long memberId;

    @Schema(description = "积分商品id", example = "赵六")
    private Long productId;

    @Schema(description = "优惠券id", example = "赵六")
    @NotNull(message = "优惠券id不能为空")
    private Long couponId;

    @Schema(description = "优惠券领取渠道 ", example = "1")
    @NotNull(message = "优惠券领取渠道不能为空")
    @InEnum(value = CouponSourceType.class,  message = "优惠券领取渠道必须是 {value}")
    private Integer couponSource;

    @Schema(description = "店铺id", example = "1")
    //@NotNull(message = "店铺id不能为空,不确定来源就传0")
    private Long storeId;

    private Integer userRestrictions;

    private Long activityId;

    private Integer sessionId;

    private Integer goodsType;

}
