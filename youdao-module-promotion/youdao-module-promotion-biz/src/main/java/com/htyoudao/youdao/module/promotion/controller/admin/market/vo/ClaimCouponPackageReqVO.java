package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(name = "领取优惠券包入参", description = "领取优惠券包入参")
public class ClaimCouponPackageReqVO {

    @Schema(description = "优惠券包id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    @Schema(description = "领取数量")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;

    @Schema(description = "活动id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long marketId;
}
