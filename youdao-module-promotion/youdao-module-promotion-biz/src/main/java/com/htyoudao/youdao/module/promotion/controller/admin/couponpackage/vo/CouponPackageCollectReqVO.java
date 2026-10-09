package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CouponPackageCollectReqVO {

    @Schema(description = "优惠卷包ID")
    private Long couponId;

    @Schema(description = "会员名称/手机号")
    private String memberName;

}
