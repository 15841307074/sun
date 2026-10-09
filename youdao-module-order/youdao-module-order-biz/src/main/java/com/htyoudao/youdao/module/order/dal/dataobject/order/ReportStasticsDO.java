package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单分析
 */
@Data
@TableName("report_stastics")
public class ReportStasticsDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 采购金额
     */
    private BigDecimal buyAmount;

    /**
     * 销售金额
     */
    private BigDecimal saleAmount;

    /**
     * 销售订单量
     */
    private Double saleNumber;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 1周, 2月
     */
    private Integer numberType;

    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间
     */
    private LocalDate endDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Integer deleted;
}

