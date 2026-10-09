package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应链 - 单品销售数据
 */
@Data
@TableName("report_commodity")
public class ReportCommodityDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 销售额
     */
    private BigDecimal totalAmount;

    /**
     * 仓库维度占比
     */
    private BigDecimal proportion;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 单品名称
     */
    private String commodityName;

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

