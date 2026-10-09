package com.htyoudao.youdao.module.system.dal.dataobject.store;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 门店费用关系表 DO
 *
 * @author ssz
 */
@TableName(value = "system_store_expenses", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStoreExpensesDO extends BaseDO {
    /**
     * 自增编号
     */
    @TableId
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 费用类型 0 堂食/外带打包费 1 外卖打包费 2 外卖配送费 3 校园配送费用
     */
    private Integer storeExpensesType;

    /**
     * 起送费/打包费限额
     */
    private BigDecimal minimumDeliveryFee;

    /**
     * 配送费/打包费
     */
    private BigDecimal additionaaCosts;

    /**
     * 费用计算方式  0 按商品  1 按订单  （ 费用类型 2 外卖配送费不考虑该字段）
     */
    private Integer storeCalculationType;


}
