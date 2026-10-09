package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 盘点单主表
 */
@Data
public class RawMaterialStocktake extends BusinessBaseDO {
    /**
     * 盘点单ID
     */
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;


    /**
     * 盘点状态(1:盘盈单 2.盘亏单)
     */
    private Integer stocktakeStatus;

    /**
     * 盘点备注
     */
    private String remark;

    /**
     * 实际出库金额
     */
    private BigDecimal outAmount;

    /**
     * 实际出库金额
     */
    private BigDecimal channelOutAmount;


    /**
     * 损耗率
     */
    private BigDecimal attritionRate;

}