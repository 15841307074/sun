package com.htyoudao.youdao.module.order.dal.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderReportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -6446964695799294865L;

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
     * 采购金额
     */
    private BigDecimal buyAmount;

    /**
     * 销售金额
     */
    private BigDecimal totalAmount;

    /**
     * 数量
     */
    private Double totalNum;

    /**
     * 比例
     */
    private BigDecimal proportion;

    /**
     * '1配送线 ，2发货线 ，3外地发货线 ，4 220度 ，5 总部线路 ，6 浪大勺 '
     */
    private Integer type;

    /**
     * 1:周 2:月
     */
    private Integer numberType;
}
