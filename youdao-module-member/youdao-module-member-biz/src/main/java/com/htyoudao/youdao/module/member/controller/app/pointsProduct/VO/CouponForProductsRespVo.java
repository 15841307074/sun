package com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.PointsProductCouponDetailVO;
import lombok.Data;

@Data
public class CouponForProductsRespVo {

    @Schema(description = "优惠卷编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    @Schema(description = "优惠卷名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "优惠卷类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponTypeName;

    @Schema(description = "优惠券图片链接", requiredMode = Schema.RequiredMode.REQUIRED)
    String couponImageUrl;

    @Schema(description = "优惠券实时完整信息")
    private PointsProductCouponDetailVO couponInfo;

}
