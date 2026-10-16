package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 货品销量异常月表 DO（只读，上游产出）
 *
 * <p>对应上游表 {@code goods_sales_anomaly_monthly}。
 * <b>出现即异常</b>：某门店的某货品在该表有一行记录 = 异常。
 * 查 distinct store_id 即可拿到异常门店集合。
 */
@TableName("goods_sales_anomaly_monthly")
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsSalesAnomalyMonthlyDO extends BusinessBaseDO {

    /**
     * 主键，上游表是 auto_increment
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 货品id
     */
    private Long goodsId;

    /**
     * 货品名称
     */
    private String goodsName;

    /**
     * 统计月份，存储当月第一天
     */
    private LocalDate statMonth;

    /**
     * 平均权重
     */
    private BigDecimal averageWeight;

    /**
     * 门店占比
     */
    private BigDecimal storeRatio;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;
}
