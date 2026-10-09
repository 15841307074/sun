package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.TreeMap;
import lombok.Data;

@Data
@Schema(description = "营销活动分析渠道数据")
public class MarketingChannelDataVO extends MarketingOverviewVO{

    private Long channelId;

    private String channelType;
    /**
     * 优惠券领取数量
     */
    @Schema(description = "优惠券领取数量", example = "20")
    private Integer couponReceiveCount;

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

    /**
     * 渠道销售额指用户通过指定渠道下单的销售额，含非活动商品订单
     */
    @Schema(description = "渠道销售额", example = "20")
    private Double channelOrderAmount;

    @Schema(description = "渠道销售额占比：渠道销售额/总销售额*100%", example = "20")
    private String channelRate;

    public MarketingChannelDataVO(TreeMap<String, Double> result) {
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
