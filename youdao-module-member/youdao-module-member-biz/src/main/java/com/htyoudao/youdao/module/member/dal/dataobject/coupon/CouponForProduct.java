package com.htyoudao.youdao.module.member.dal.dataobject.coupon;

import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GoodCouponVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分商品关联的优惠券信息。
 */
@Data
public class CouponForProduct {

    @Schema(description = "优惠券编码")
    private String couponCode;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型名称")
    private String couponTypeName;

    @Schema(description = "优惠券图片链接")
    private String couponImageUrl;

    @Schema(description = "优惠券实时完整信息，仅详情接口返回")
    private GoodCouponVO couponInfo;
}
