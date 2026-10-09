package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order;

import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnalysisOrderVO {

    /**
     * 有效订单
     */
    @Schema(description = "有效订单")
    private Long validOrders;
    /**
     * 堂食订单数
     */
    @Schema(description = "堂食订单数")
    private Long canteenFoodOrders;

    /**
     * 外卖订单数
     */
    @Schema(description = "外卖订单数")
    private Long takeawayOrders;

    /**
     * 外带订单数
     */
    @Schema(description = "外带订单数")
    private Long packOrders;

    /**
     * 下单顾客数
     */
    @Schema(description = "下单顾客数")
    private Long customerCount;

    /**
     * 客单价
     */
    @Schema(description = "客单价")
    private Double averagePayment;

    /**
     * 实收额
     */
    @Schema(description = "实收额")
    private Double payAmount = 0.0;


    /**
     * 现金收款订单数 == 不付款下单订单数
     */
    @Schema(description = "现金收款订单数 == 不付款下单订单数")
    private Long cashPayOrders;

    /**
     * 无效订单数
     */
    @Schema(description = "无效订单数")
    private Long invalidOrders;

    /**
     * UV
     */
    @Schema(description = "进店UV")
    private Long uv;


    /**
     * 下单转化率
     */
    @Schema(description = "下单转化率")
    private BigDecimal orderCountRate;



    public AnalysisOrderVO(Map<String, Double> result) {
        if (result == null) {
            result = new java.util.HashMap<>();
        }
        this.payAmount = result.getOrDefault("payAmount", 0.0);
        this.canteenFoodOrders = result.getOrDefault("canteenFoodOrders", 0.0).longValue();
        this.takeawayOrders = result.getOrDefault("takeawayOrders", 0.0).longValue();
        this.packOrders = result.getOrDefault("packOrders", 0.0).longValue();
        this.validOrders = result.getOrDefault("validOrders", 0.0).longValue();
        this.customerCount = result.getOrDefault("customerCount", 0.0).longValue();
        this.averagePayment = result.getOrDefault("averagePayment", 0.0);
        this.cashPayOrders = result.getOrDefault("cashPayOrders", 0.0).longValue();
        this.invalidOrders = result.getOrDefault("invalidOrders", 0.0).longValue();
        this.uv = result.getOrDefault("uv", 0.0).longValue();
        calculateRates();
    }


    private void calculateRates() {
        if (uv == null || uv == 0L) {
            this.orderCountRate = BigDecimal.ZERO;
            return;
        }

        this.orderCountRate = NumberUtil.div(customerCount, uv);
    }



}