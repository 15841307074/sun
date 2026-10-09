package com.htyoudao.youdao.module.commodity.dal.dataobject;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU 对象 commodity_skus
 * 
 * @author Qizhongnan
 * @date 2024-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommoditySkus extends BusinessBaseDO
{
    private static final long serialVersionUID = 1L;

    /** SKU ID */

    @TableId(value = "sku_id",type= IdType.ASSIGN_ID)
    private Long skuId;

    /** 关联商品 ID */

    private Long commodityId;

    /** 是否是门店 1 是 2 否 弃用*/
    private Integer isStore;

    /** SKU 编码 */
    private String skuCode;

    /** 规格名称 */
    private String skusName;

    /** 规格值 */
    private String skusValue;



    /**
     *  门店状态
     *  1上架 0下架
     */
    private int storeStatus;
    /**
     *  微信状态 暂时弃用，字段保留
     *
     */
    private int wxStatus;

    /**
     * 价格
     */
    private BigDecimal illustratePrices;
    /**
     * 划线价格
     */
    private BigDecimal strikeThroughPrice;





}
