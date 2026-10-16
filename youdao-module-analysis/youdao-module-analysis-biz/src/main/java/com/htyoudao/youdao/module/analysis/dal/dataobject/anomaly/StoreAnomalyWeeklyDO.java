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
 * <b>出现即异常</b>：门店在该表有一行记录 = 该门店本周有异常。
 *
 * <p>非空的指标列 = 该门店涉及的异常指标，
 * 用于计算 anomaly_metric_count。
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

    // ==================== 指标列（非空 = 该门店涉及这个异常指标） ====================

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
}
