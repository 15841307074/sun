package com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity;


import lombok.Data;

import java.io.Serializable;

/**
 * 商品 SKU 对象 commodity_skus
 *
 * @author Qizhongnan
 * @date 2024-01-16
 */
@Data
public class CommoditySkusVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */

    private Long skuId;

    /**
     * 关联商品 ID
     */

    private Long commodityId;

    /**
     * 是否是门店 1 是 2 否
     */
    private Integer isStore;

    /**
     * SKU 编码
     */
    private String skuCode;

    /**
     * 规格名称
     */
    private String skusName;

    /**
     * 规格值
     */
    private String skusValue;

    /**
     * 商品模板价格表
     */
//    private CommodityIllustratePricesDTO commodityIllustratePrices;

    /**
     * 商品门店价格表
     */
//    private CommodityStorePricesDTO commodityStorePrices;

}
