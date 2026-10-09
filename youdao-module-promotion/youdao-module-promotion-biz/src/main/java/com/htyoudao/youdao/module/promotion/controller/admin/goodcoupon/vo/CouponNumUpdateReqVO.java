package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * @author dht
 */
@Data
@Schema(name = "修改优惠券数量入参", description = "修改优惠券数量入参")
@ToString(callSuper = true)
public class CouponNumUpdateReqVO {

    @Schema(description = "优惠券id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;


    @Schema(description = "修改数量")
    private Integer num;
}
