package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

@Data
public class AnalysisMemberVO {

    /**
     * 下单顾客数
     */
    @Schema(description = "下单顾客数")
    private Long customerCount;

    /**
     * 复购率
     */
    @Schema(description = "复购率")
    private BigDecimal repeatBuyersRate;


    /**
     * 复购人数
     */
    @Schema(description = "复购人数")
    private Long repeatBuyers;

    /**
     * 下单新客数
     */
    @Schema(description = "下单新客数")
    private Long newCustomerCount;

    /**
     * 下单老客数
     */
    @Schema(description = "下单老客数")
    private Long oldCustomerCount;

    /**
     * 会员数
     */
    @Schema(description = "会员数")
    private Long memberCustomerCount;


    public AnalysisMemberVO(Map<String, Double> result) {
        if (result != null) {
            this.customerCount = result.getOrDefault("customerCount", 0.0).longValue();
            this.repeatBuyers = result.getOrDefault("repeatBuyers", 0.0).longValue();
            this.newCustomerCount = result.getOrDefault("newCustomerCount", 0.0).longValue();
            this.oldCustomerCount = result.getOrDefault("oldCustomerCount", 0.0).longValue();
            this.memberCustomerCount = result.getOrDefault("memberCustomerCount", 0.0).longValue();
            this.repeatBuyers = result.getOrDefault("repeatBuyers", 0.0).longValue();
        }
        calculateRates();
    }

    private void calculateRates() {
        if (customerCount == null || customerCount == 0L) {
            this.repeatBuyersRate = BigDecimal.ZERO;
            return;
        }

        this.repeatBuyersRate = NumberUtil.div(repeatBuyers, customerCount);
    }

}
