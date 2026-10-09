package com.htyoudao.youdao.module.order.controller.app.report.VO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReportListRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 销售额占比
     */
    private BigDecimal bProportion;

    /**
     * 总体销售额
     */
    private BigDecimal bTotalAmount;

    /**
     * 采购额
     */
    private BigDecimal bBuyTotalAmount;

    /**
     * 销售门店数
     */
    private Double bTotalNum;

    /**
     * 平均销售额
     */
    private BigDecimal bAvgAmount;

    /**
     * 总体销售额
     */
    private BigDecimal sTotalAmount;

    /**
     * 采购额
     */
    private BigDecimal sBuyTotalAmount;

    /**
     * 销售门店数
     */
    private Double sTotalNum;

    /**
     * 平均销售额
     */
    private BigDecimal sAvgAmount;

    /**
     * 销售额占比
     */
    private BigDecimal sProportion;

    /**
     * 类型：
     * 1 - 配送线
     * 2 - 发货线
     * 3 - 外地发货线
     * 4 - 220度
     * 5 - 总部线路
     * 6 - 浪大勺
     */
    private Integer type;

    /**
     * 销售额环比
     */
    private BigDecimal totalAmountMonthOnMonth;

    /**
     * 订单量环比
     */
    private BigDecimal totalNumMonthOnMonth;

    /**
     * 采购量环比
     */
    private BigDecimal totalBuyAmountMonthOnMonth;

    private Long cnt50_100 = 0L;

    private Long cnt100_200 = 0L;

    private Long cnt200Plus = 0L;

    private String startDate0;

    private String endDate0;

    private String startDate1;

    private String endDate1;
}
