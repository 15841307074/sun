package com.htyoudao.youdao.module.promotion.controller.app.couponpackage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Data
public class ClaimCouponPackageReqVO {

    @Schema(description = "优惠券包id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    @Schema(description = "memberId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

    @Schema(description = "活动id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long marketId;

    @Schema(description = "店铺id", example = "1")
    @NotNull(message = "店铺id不能为空,不确定来源就传0")
    private Long storeId;

    @Schema(description = "优惠券领取渠道 ", example = "1")
    @NotNull(message = "优惠券领取渠道不能为空,不知道就编")
    @InEnum(value = CouponSourceType.class,  message = "优惠券领取渠道必须是 {value}")
    private Integer couponSource;
}
