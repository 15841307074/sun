package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应链 - 仓库销售数据
 */
@Data
@TableName("report_warehouse")
public class ReportWarehouseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7342709889565143224L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private BigDecimal totalAmount;

    private Double totalNum;

    private String warehouseName;

    private Integer numberType;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createTime;

    private Integer deleted;
}
