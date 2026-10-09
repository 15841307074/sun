package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券适用商品信息。
 */
@Data
@Schema(description = "优惠券适用商品信息")
public class CouponCommodityInfoVO implements Serializable {

    @Schema(description = "商品ID")
    private Long commodityId;

    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "适用类型：2指定可用，3指定不可用")
    private Integer type;
}
