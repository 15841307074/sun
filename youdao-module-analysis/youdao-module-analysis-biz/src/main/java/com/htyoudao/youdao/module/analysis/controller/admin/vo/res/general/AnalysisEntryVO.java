package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.general;

import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnalysisEntryVO {

    /**
     * 营收额
     */
    @Schema(description = "营收额")
    private Double orderAmount = 0.0;

    /**
     * 实收额
     */
    @Schema(description = "实收额")
    private Double payAmount = 0.0;

    /**
     * 优惠总额
     */
    @Schema(description = "优惠总额")
    private Double discountAmount = 0.0;

    /**
     * 优惠后总额
     */
    @Schema(description = "优惠后总额")
    private Double afterDiscountAmount = 0.0;

    /**
     * 堂食订单优惠前总额
     */
    @Schema(description = "堂食订单优惠前总额")
    private Double canteenFoodOrderAmount = 0.0;

    /**
     * 外卖订单优惠前总额
     */
    @Schema(description = "外卖订单优惠前总额")
    private Double takeawayOrderAmount;

    /**
     * 外带订单优惠前总额
     */
    @Schema(description = "外带订单优惠前总额")
    private Double packOrderAmount;

    /**
     * 现金收款
     */
    @Schema(description = "现金收款")
    private Double cashPayAmount;

    /**
     * 有效订单
     */
    @Schema(description = "有效订单")
    private Long validOrders;

    /**
     * 复购人数
     */
    @Schema(description = "复购人数")
    private Long repeatBuyers;

    /**
     * 下单顾客数
     */
    @Schema(description = "下单顾客数")
    private Long customerCount;

    /**
     * UV
     */
    @Schema(description = "进店UV")
    private Long uv;

    /**
     * 复购率
     */
    @Schema(description = "复购率")
    private BigDecimal repeatBuyersRate;

    /**
     * 下单转化率
     */
    @Schema(description = "下单转化率")
    private BigDecimal orderCountRate;


    /**
     * 实际到账金额
     */
    @Schema(description = "实际到账金额")
    private Double netrAmt;


    public AnalysisEntryVO(Map<String, Double> result) {
        if (result == null) {
            result = new java.util.HashMap<>();
        }
        this.orderAmount = result.getOrDefault("orderAmount", 0.0);
        this.payAmount = result.getOrDefault("payAmount", 0.0);
        this.discountAmount = result.getOrDefault("discountAmount", 0.0);
        this.afterDiscountAmount = result.getOrDefault("afterDiscountAmount", 0.0);
        this.canteenFoodOrderAmount = result.getOrDefault("canteenFoodOrderAmount", 0.0);
        this.takeawayOrderAmount = result.getOrDefault("takeawayOrderAmount", 0.0);
        this.packOrderAmount = result.getOrDefault("packOrderAmount", 0.0);
        this.cashPayAmount = result.getOrDefault("cashPayAmount", 0.0);
        this.validOrders = result.getOrDefault("validOrders", 0.0).longValue();
        this.repeatBuyers = result.getOrDefault("repeatBuyers", 0.0).longValue();
        this.customerCount = result.getOrDefault("customerCount", 0.0).longValue();
        this.uv = result.getOrDefault("uv", 0.0).longValue();
        this.netrAmt = result.getOrDefault("netrAmt", 0.0);

        calculateRates();
    }


    private void calculateRates() {
        if (customerCount == null || customerCount == 0L) {
            this.repeatBuyersRate = BigDecimal.ZERO;
        }else {
            this.repeatBuyersRate = NumberUtil.div(repeatBuyers, customerCount);
        }

        if (uv == null || uv == 0L){
            this.orderCountRate = BigDecimal.ZERO;
        }else {
            this.orderCountRate = NumberUtil.div(customerCount, uv);
        }

    }



}