package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import lombok.Data;

@Data
public class AnalysisMemberVisitOrderVO {

    /**
     * 新用户进店人数
     */
    @Schema(description = "新用户进店人数")
    private Long newVisitorCount = 0L;

    /**
     * 老用户进店人数
     */
    @Schema(description = "老用户进店人数")
    private Long oldVisitorCount = 0L;

    /**
     * 新用户下单人数
     */
    @Schema(description = "新用户下单人数")
    private Long newCustomerCount = 0L;

    /**
     * 老用户下单人数
     */
    @Schema(description = "老用户下单人数")
    private Long oldCustomerCount = 0L;


    /**
     * 新用户下单进店比例
     */
    @Schema(description = "新用户下单进店比例")
    private BigDecimal newOrderCustomerRate = BigDecimal.ZERO;

    /**
     * 老用户下单进店比例
     */
    @Schema(description = "老用户下单进店比例")
    private BigDecimal oldOrderCustomerRate  = BigDecimal.ZERO;

    public AnalysisMemberVisitOrderVO(Map<String, Double> result) {
        if (result != null) {
            this.newCustomerCount = result.getOrDefault("newCustomerCount", 0.0).longValue();
            this.oldCustomerCount = result.getOrDefault("oldCustomerCount", 0.0).longValue();
        }
    }

    public void calculateRates() {
        if (newVisitorCount != null && newVisitorCount != 0L) {
            this.newOrderCustomerRate = NumberUtil.div(newCustomerCount, newVisitorCount);
        }
        if (oldVisitorCount != null && oldVisitorCount != 0L) {
            this.oldOrderCustomerRate = NumberUtil.div(oldCustomerCount, oldVisitorCount);
        }
    }

}