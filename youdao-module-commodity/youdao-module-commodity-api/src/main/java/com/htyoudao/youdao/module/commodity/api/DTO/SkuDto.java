package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -8919053060805074555L;

    private Long skuId;
    private String skuName;
    private String skuValue;
    private BigDecimal skuPrice;
    /**
     * 门店下当前规格的状态
     */
    private Integer commodityStoreSkuStatus;
    /**
     * 门店下当前规格的划线价格
     */
    private BigDecimal commodityStoreSkuStrikePrice;

    //顺序
    Integer sort;
}