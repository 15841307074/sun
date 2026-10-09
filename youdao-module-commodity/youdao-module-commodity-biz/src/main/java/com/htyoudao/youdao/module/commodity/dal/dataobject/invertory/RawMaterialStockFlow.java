package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 原材料库存变动明细表
 */
@Data
public class RawMaterialStockFlow extends BusinessBaseDO {
    /**
     * 流水记录唯一ID
     */
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 关联的原材料ID
     */
    private Long rawMaterialId;

    /**
     * 变动类型：销售/采购入库/调整/报损/盘点/调拨出库/调拨入库/取消订单返还库存
     */
    private String changeType;

    /**
     * 变动数量（正数表示增加，负数表示减少）
     */
    private BigDecimal changeQuantity;

//    /**
//     * 规格
//     */
//    private String specifications;
//
//    /**
//     * 规格单位换算规则
//     */
//    private String specificationsRules;
    /**
     * 原材料快照
     */
    private String materialSnapshot;

    /**
     * 变动前的库存数量
     */
    private BigDecimal stockBefore;

    /**
     * 变动后的库存数量
     */
    private BigDecimal stockAfter;

    /**
     * 最小单位单价
     */
    private BigDecimal unitPrice;

    /**
     * 最小单位
     */
    private String minUnit;


    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 选择单位
     */
    private String chooseUnit;

    /**
     * 关联业务单号（如订单编号，盘点编号）
     */
    private String referenceId;

    /**
     * 备注（如：损耗原因、调整说明等）
     */
    private String notes;

}