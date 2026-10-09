package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户优惠券请求参数
 * @author dht
 */
@Data
public class UserCouponReqVO {

    @Schema(description = "用户优惠券id")
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "用户优惠券id不能为空")
    private Long userCouponId;

    @Schema(description = "优惠券id")
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "优惠券id不能为空")
    private Long couponId;

    @Schema(description = "会员id")
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "会员id不能为空")
    private Long memberId;

    @Schema(description = "门店id")
    @NotNull(message = "门店id不能为空")
    private String storeId;
}
