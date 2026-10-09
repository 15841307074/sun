package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 模板商品规格表
 * </p>
 *
 * @author ssz
 * @since 2025-02-05
 */
@Data
@TableName("commodity_template_skus")
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateSkus extends BusinessBaseDO implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "template_sku_id", type = IdType.AUTO)
    private Long templateSkuId;

    private Long commodityId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 关联模板商品 ID
     */
    private Long commodityTemplateId;

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
     * 是否是门店 1 是 2 否(弃用）
     */
    private Integer isStore;

    /**
     * 门店上架状态
     */
    private Integer storeStatus;

    /**
     * 小程序上下架状态
     */
    private Integer wxStatus;


    /**
     * 平台价格
     */
    private BigDecimal illustratePrices;
    /**
     * 划线价格
     */
    private BigDecimal strikeThroughPrice;


}
