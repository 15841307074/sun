package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 门店异常指标周表 DO（只读，上游产出）
 *
 * <p>对应上游表 {@code store_anomaly_metrics_weekly}。
 * 同时保存投诉及有效订单统计，仅有正常订单、投诉为零的行不属于异常。
 *
 * <p>其他经营指标仍按非空判断异常；投诉统计为零时不计入 anomaly_metric_count。
 */
@TableName("store_anomaly_metrics_weekly")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreAnomalyWeeklyDO extends BusinessBaseDO {

    /**
     * 主键，上游表是 auto_increment
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 统计周所属年份
     */
    private Integer statYear;

    /**
     * ISO周序号 1-53
     */
    private Integer statWeek;

    // ==================== 经营异常指标和投诉统计 ====================

    /**
     * 周营业额
     */
    private BigDecimal weeklyTurnover;

    /**
     * 前一周营业额
     */
    private BigDecimal prevWeekTurnover;

    /**
     * 周客单价变动率
     */
    private BigDecimal weeklyAvgOrderAmountChangeRate;

    /**
     * 周客单价
     */
    private BigDecimal weeklyAvgOrderAmount;

    /**
     * 前一周客单价
     */
    private BigDecimal prevWeekAvgOrderAmount;

    /**
     * 客单价变动金额
     */
    private BigDecimal avgOrderAmountChange;

    /**
     * 本周复购人数
     */
    private Long weeklyRepeatCustomerCount;

    /**
     * 本周下单人数
     */
    private Long weeklyOrderCustomerCount;

    /**
     * 周餐品销量
     */
    private Long weeklyMealSalesQuantity;

    /**
     * 前一周餐品销量
     */
    private Long prevWeekMealSalesQuantity;

    /**
     * 投诉订单占比
     */
    private BigDecimal complaintOrderRatio;

    /**
     * 投诉订单数
     */
    private Long complaintOrderCount;

    /**
     * 门店有效订单数
     */
    private Long validOrderCount;

    /**
     * 食卫投诉订单数
     */
    private Long foodHygieneComplaintOrderCount;

    /** 仅有正常投诉统计的行不进入异常报告；其他任务的异常指标判断保持原口径。 */
    public boolean hasOnlyNormalComplaintStatistics() {
        boolean hasStatistics = complaintOrderRatio != null || complaintOrderCount != null
                || validOrderCount != null || foodHygieneComplaintOrderCount != null;
        return hasStatistics
                && (complaintOrderRatio == null || complaintOrderRatio.signum() <= 0)
                && (complaintOrderCount == null || complaintOrderCount <= 0)
                && (foodHygieneComplaintOrderCount == null || foodHygieneComplaintOrderCount <= 0)
                && weeklyTurnover == null && prevWeekTurnover == null
                && weeklyAvgOrderAmountChangeRate == null && weeklyAvgOrderAmount == null
                && prevWeekAvgOrderAmount == null && avgOrderAmountChange == null
                && weeklyRepeatCustomerCount == null && weeklyOrderCustomerCount == null
                && weeklyMealSalesQuantity == null && prevWeekMealSalesQuantity == null;
    }
}
