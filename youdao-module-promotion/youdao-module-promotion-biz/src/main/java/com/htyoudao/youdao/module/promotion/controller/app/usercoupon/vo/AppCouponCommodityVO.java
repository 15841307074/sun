package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "优惠卷可用商品VO")
public class AppCouponCommodityVO {

    @Schema(description = "商品缩略图")
    private String imageUrl;

    @Schema(description = "商品名称")
    private String name;
}
