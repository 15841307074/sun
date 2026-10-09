package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 下单时商品入参
 * @author dht
 */
@Schema(name = "下单时商品入参", description = "下单时商品入参")
@Data
public class OrderGoods implements Serializable {

    @Schema(description = "金额")
    private BigDecimal transactionAmount;

    @Schema(description = "商品id")
    private Long commodityId;

    @Schema(description = "活动id")
    private Long activityId;
}