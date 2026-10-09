package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "营销活动分析概览数据")
@NoArgsConstructor
public class MarketingOverviewVO {

    /**
     * 活动页面被访问总次数之和（同一用户多次访问累加）
     */
    @Schema(description = "活动页面被访问总次数之和（同一用户多次访问累加）", example = "32")
    private Long linkClickCount;

    /**
     * 活动页面不同用户访问数之和（去重）
     */
    @Schema(description = "活动页面不同用户访问数之和（去重）", example = "6")
    private Long linkClickUserCount;

    /**
     * 活动期间下单购买活动商品的用户数之和（去重）
     */
    @Schema(description = "活动期间下单购买活动商品的用户数之和（去重）", example = "32")
    private Integer customerCount;

    /**
     * 活动订单数之和（不含已退款、取消订单）
     */
    @Schema(description = "支付次数（不含已退款、取消订单）", example = "32")
    private Integer orderCount;


    /**
     * 购买商品件数
     */
    @Schema(description = "购买商品件数", example = "54")
    private Integer salesVolume;

    /**
     * 活动优惠
     */
    @Schema(description = "优惠券优惠金额", example = "60")
    private Double activityDiscountAmount;

    @Schema(description = "活动优惠金额", example = "100")
    private Double promotionDiscountAmount;

    @Schema(description = "总优惠金额", example = "160")
    private Double allDiscountAmount;

    /**
     * 活动销售额
     */
    @Schema(description = "活动销售额", example = "160")
    private Double salesAmount;


    /**
     * 活动销售额 / 订单数 * 100%
     */
    @Schema(description = "客单价（活动销售额 / 订单数 * 100%）", example = "10.00")
    private Double customerUnitPrice;

    /**
     * 下单人数 / 链接点击人数 * 100%
     */
    @Schema(description = "下单人数 / 链接点击人数 * 100%", example = "54%")
    private String conversionRate;


    /**
     * 支付次数 / 链接点击次数 * 100%
     */
    @Schema(description = "支付次数 / 链接点击次数 * 100%", example = "54%")
    private String conversionPayRate;



    public MarketingOverviewVO(Map<String, Double> result) {
        if (result == null) {
            result = new HashMap<>();
        }

        this.setCustomerCount(result.getOrDefault("customerCount", 0.0).intValue());
        this.setOrderCount(result.getOrDefault("orderCount", 0.0).intValue());
        this.setSalesVolume(result.getOrDefault("salesVolume", 0.0).intValue());
        this.setActivityDiscountAmount(result.getOrDefault("activityDiscountAmount", 0.0));
        this.setPromotionDiscountAmount(result.getOrDefault("promotionDiscountAmount", 0.0));
        this.setAllDiscountAmount(result.getOrDefault("allDiscountAmount", 0.0));
        this.setSalesAmount(result.getOrDefault("payAmount", 0.0));
    }


    public void setRate() {
        if (this.orderCount != null && this.orderCount != 0 && this.salesAmount != null) {
            this.customerUnitPrice = this.salesAmount / this.orderCount;
        } else {
            this.customerUnitPrice = 0.0;
        }

        if (this.linkClickUserCount != null && this.linkClickUserCount != 0 && this.customerCount != null) {
            double rate = (double) this.customerCount / this.linkClickUserCount;
            this.conversionRate = String.format("%.2f%%", rate * 100);
        } else {
            this.conversionRate = "0.00%";
        }

        if (this.linkClickCount != null && this.linkClickCount != 0 && this.orderCount != null) {
            double rate = (double) this.orderCount / this.linkClickCount;
            this.conversionPayRate = String.format("%.2f%%", rate * 100);
        } else {
            this.conversionPayRate = "0.00%";
        }
    }
}
