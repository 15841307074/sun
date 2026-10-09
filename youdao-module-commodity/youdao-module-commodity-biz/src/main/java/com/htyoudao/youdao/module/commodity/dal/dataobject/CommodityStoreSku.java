package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 门店下商品规格表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Getter
@Setter
@TableName("commodity_store_sku")
@EqualsAndHashCode(callSuper = true)
public class CommodityStoreSku extends BusinessBaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店下商品的SKUID
     */
    @TableId(value = "commodity_store_sku_id", type = IdType.ASSIGN_ID)
    private Long commodityStoreSkuId;

    /**
     * 商品id
     */
    private Long commodityId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店下商品的ID
     */

    private Long commodityStoreSpuId;

    /**
     * 门店下规格名称
     */
    private String commodityStoreSkuName;

    /**
     * 门店下规格的值
     */
    private String commodityStoreSkuValue;

    /**
     * 门店下当前规格的售卖价格
     */
    private BigDecimal commodityStoreSkuPrice;

    /**
     * 门店下当前规格的划线价格
     */
    private BigDecimal commodityStoreSkuStrikePrice;

    /**
     * 门店下当前规格的库存
     */
    private Long commodityStoreSkuStockQuantity;

    /**
     * 门店下当前规格的状态
     */
    private Integer commodityStoreSkuStatus;


    private Long skuId;


}
