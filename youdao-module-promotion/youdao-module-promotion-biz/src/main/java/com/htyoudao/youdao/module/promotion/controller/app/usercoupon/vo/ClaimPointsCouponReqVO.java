package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "app - 领取集点优惠券参数 Request VO")
@Data
public class ClaimPointsCouponReqVO {

    @Schema(description = "memberId", example = "赵六")
    @NotNull(message = "memberId不能为空")
    private Long memberId;

    @Schema(description = "优惠券id", example = "赵六")
    @NotNull(message = "优惠券id不能为空")
    private Long couponId;

    @Schema(description = "店铺id", example = "1")
    @NotNull(message = "店铺id不能为空,不确定来源就传0")
    private Long storeId;

    @Schema(description = "用户类型", example = "1")
    @NotNull(message = "用户类型不能为空,不确定就传0")
    private Integer userRestrictions;

    @Schema(description = "活动id", example = "1")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "兑换类型 1优惠券 2券包", example = "1")
    @NotNull(message = "兑换类型不能为空")
    private Integer goodsType;
}
