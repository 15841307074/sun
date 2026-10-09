package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CalculateCacheDataCopyDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: dht
 */
@Data
public class SettlementReqVO {
    /**
     * 指定门店id
     */
    @Schema(description = "指定门店id")
    private Long storeId;

    @Schema(description = "下单时商品入参")
    private List<OrderGoods> goodsList;

    @Schema(description = "userId")
    private Long userId;

    /**
     * 交易金额
     */
    @Schema(description = "交易金额")
    private BigDecimal transactionAmount;

    @Schema(description = "用餐方式 0 堂食 1 打包 2 外卖")
    private Integer habit;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userCouponId;

    @Schema(description = "商品数量-后端用")
    private int goodSize;

    /**
     * 是否支持单个优惠券计算
     */
    @Schema(description = "是否支持单个优惠券计算")
    private Boolean isSupportSingle = false;

    /**
     * 商品信息集合
     */
    @Schema(description = "是否支持单个优惠券计算")
    private List<CalculateCacheDataCopyDTO.CommodityInfoVO> commodityInfos;
}
