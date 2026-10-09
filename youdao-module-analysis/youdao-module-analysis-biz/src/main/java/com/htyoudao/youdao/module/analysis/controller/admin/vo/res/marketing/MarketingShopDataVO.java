package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.TreeMap;
import lombok.Data;

@Data
@Schema(description = "营销活动分析门店数据")
public class MarketingShopDataVO extends MarketingOverviewVO{

    /**
     * 门店ID
     */
    @Schema(description = "门店ID", example = "1001")
    private Long storeId;

    /**
     * 门店名称
     */
    @Schema(description = "门店名称", example = "江和美店")
    private String storeName;

    /**
     * 优惠券领取数量
     */
    @Schema(description = "优惠券领取数量", example = "20")
    private Long couponReceiveCount;

    /**
     * 优惠券使用数量
     */
    @Schema(description = "优惠券使用数量", example = "20")
    private Integer couponUseCount;

    /**
     * 优惠券使用数量 / 领取数量 * 100%
     */
    @Schema(description = "优惠券使用数量 / 领取数量 * 100%", example = "20%")
    private String couponUseRate;

    public MarketingShopDataVO(TreeMap<String, Double> result) {
        if (result == null) {
            result = new TreeMap<>();
        }
        this.setCustomerCount(result.getOrDefault("customerCount", 0.0).intValue());
        this.setOrderCount(result.getOrDefault("orderCount", 0.0).intValue());
        this.setSalesVolume(result.getOrDefault("salesVolume", 0.0).intValue());
        this.setActivityDiscountAmount(result.getOrDefault("activityDiscountAmount", 0.0));
        this.setPromotionDiscountAmount(result.getOrDefault("promotionDiscountAmount", 0.0));
        this.setAllDiscountAmount(result.getOrDefault("allDiscountAmount", 0.0));
        this.setSalesAmount(result.getOrDefault("payAmount", 0.0));
        this.setCouponUseCount(result.getOrDefault("couponUseCount", 0.0).intValue());
    }

}  
