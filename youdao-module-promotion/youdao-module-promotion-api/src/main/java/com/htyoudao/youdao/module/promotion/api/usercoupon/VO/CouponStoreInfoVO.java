package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券适用门店信息。
 */
@Data
@Schema(description = "优惠券适用门店信息")
public class CouponStoreInfoVO implements Serializable {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "组织ID")
    private Long deptId;

    @Schema(description = "门店标签ID")
    private Long tagId;
}
