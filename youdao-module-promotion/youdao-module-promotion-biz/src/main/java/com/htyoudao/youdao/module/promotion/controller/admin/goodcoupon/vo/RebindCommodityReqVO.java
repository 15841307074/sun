package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(name = "优惠券重新绑定商品入参", description = "优惠券重新绑定商品入参")
@ToString(callSuper = true)
public class RebindCommodityReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    @NotNull(message = "isCommonStore不能为空")
    private Integer isCommonStore;

    @Schema(description = "优惠券适用商品")
    @NotNull(message = "优惠券适用商品不能为空")
    List<CouponCommodityDO> couponCommodities;

    @Schema(description = "优惠券适用门店")
    List<CouponStoreDO> couponStores;
}
