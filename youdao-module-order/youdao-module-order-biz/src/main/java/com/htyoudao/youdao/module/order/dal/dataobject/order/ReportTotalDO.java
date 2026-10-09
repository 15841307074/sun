package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应链 - 总体销售数据
 */
@TableName("report_total")
@Data
public class ReportTotalDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -79713846189558780L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 总体销售额
     */
    private BigDecimal totalAmount;

    /**
     * 销售门店数
     */
    private Double totalNum;

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
