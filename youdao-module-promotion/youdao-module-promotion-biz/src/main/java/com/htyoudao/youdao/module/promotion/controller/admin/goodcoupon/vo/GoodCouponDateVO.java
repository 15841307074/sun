package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dht
 * 初始版本 优惠券数据
 */
@Schema(name = "优惠券数据返回参", description = "优惠券数据返回参")
@Data
public class GoodCouponDateVO {

    @Schema(description = "用券总成交额")
    private BigDecimal turnover;

    @Schema(description = "优惠总金额")
    private BigDecimal offerTotal;

    @Schema(description = "费效比")
    private BigDecimal cost;

    @Schema(description = "付款单数")
    private Integer orderNum;

    @Schema(description = "用券笔单价")
    private BigDecimal singlePrice;

    @Schema(description = "老客户数量")
    private Integer oldCustom;

    @Schema(description = "新客户数量")
    private Integer newCustom;

    @Schema(description = "商品数量")
    private Integer itemNum;

    @Schema(description = "使用率")
    private BigDecimal usedRate;

    @Schema(description = "店铺id")
    private Long storeId;

    @Schema(description = "店铺名称")
    private String storeName;
}
