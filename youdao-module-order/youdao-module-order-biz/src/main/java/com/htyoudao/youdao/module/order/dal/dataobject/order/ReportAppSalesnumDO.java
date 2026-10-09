package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 小程序在售门店数
 */
@TableName("report_app_salesnum")
@Data
public class ReportAppSalesnumDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 销售门店数
     */
    private Double saleNumber;

    /**
     * 统计类型：
     * 1 - 周
     * 2 - 月
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
     * 是否删除：0 - 否，1 - 是
     */
    private Integer deleted;
}
