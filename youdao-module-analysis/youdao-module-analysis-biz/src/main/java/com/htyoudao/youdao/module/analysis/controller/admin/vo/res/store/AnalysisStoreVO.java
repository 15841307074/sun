package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store;

import cn.hutool.core.util.NumberUtil;
import com.htyoudao.youdao.framework.common.util.collection.MapUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@NoArgsConstructor
public class AnalysisStoreVO {


    /**
     * 实收额
     */
    @Schema(description = "实收额")
    private Double payAmount;

    /**
     * 优惠前总额
     */
    @Schema(description = "优惠前总额")
    private Double orderAmount;


    /**
     * 点餐实收额
     */
    @Schema(description = "点餐实收额")
    private Double dcPayAmount;

    /**
     * 有效订单
     */
    @Schema(description = "有效订单")
    private Long validOrders;

    /**
     * 下单顾客数
     */
    @Schema(description = "下单顾客数")
    private Long customerCount;


    @Schema(description = "无效订单数")
    private Long invalidOrders;

    /**
     * UV
     */
    @Schema(description = "进店UV")
    private Long uv;

    /**
     * 复购人数
     */
    @Schema(description = "复购人数")
    private Long repeatBuyers;


    /**
     * 客单价
     */
    @Schema(description = "客单价")
    private Double averagePayment;

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
     * 新客占比
     */
    @Schema(description = "新客占比")
    private BigDecimal newCustomerRate;

    /**
     * 新客人数
     */
    @Schema(description = "新客人数")
    private Long newCustomerCount;

    /**
     * 会员数
     */
    @Schema(description = "会员数")
    private Long memberCustomerCount;

    /**
     * 实际到账金额
     */
    @Schema(description = "实际到账金额")
    private Double netrAmt;

    /**
     * 供应链订货总额
     */
    @Schema(description = "供应链订货总额")
    private Double gylOrderAmount;


    public AnalysisStoreVO(Map<String, Double> result) {
        if (result == null) {
            result = new HashMap<>();
        }
        this.uv = result.getOrDefault("uv", 0.0).longValue();
        this.customerCount = result.getOrDefault("customerCount", 0.0).longValue();
        this.repeatBuyers = result.getOrDefault("repeatBuyers", 0.0).longValue();
        this.newCustomerCount = result.getOrDefault("newCustomerCount", 0.0).longValue();
        this.payAmount = result.getOrDefault("payAmount", 0.0);
        this.orderAmount = result.getOrDefault("orderAmount", 0.0);
        this.dcPayAmount = result.getOrDefault("dcPayAmount", 0.0);
        this.validOrders = result.getOrDefault("validOrders", 0.0).longValue();
        this.invalidOrders = result.getOrDefault("invalidOrders", 0.0).longValue();
        this.averagePayment = result.getOrDefault("averagePayment", 0.0);
        this.memberCustomerCount = result.getOrDefault("memberCustomerCount", 0.0).longValue();
        this.netrAmt = result.getOrDefault("netrAmt", 0.0);
        calculateRates();
    }

    private void calculateRates() {
        //计算各种率
        if (Objects.isNull(this.customerCount) || this.customerCount == 0) {
            this.setRepeatBuyersRate(BigDecimal.ZERO);
            this.setNewCustomerRate(BigDecimal.ZERO);
        }else {
            this.setRepeatBuyersRate((NumberUtil.div(this.getRepeatBuyers(), this.getCustomerCount())));
            this.setNewCustomerRate((NumberUtil.div(this.getNewCustomerCount(), this.getCustomerCount())));
        }
        if (Objects.isNull(this.uv) || this.uv == 0) {
            this.setOrderCountRate(BigDecimal.ZERO);
        }else {
            this.setOrderCountRate((NumberUtil.div(this.getCustomerCount(), this.getUv())));
        }
    }


}