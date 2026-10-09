package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 门店下商品套餐分组里的单品表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Getter
@Setter
@TableName("commodity_store_single")
@EqualsAndHashCode(callSuper = true)
public class CommodityStoreSingle extends BusinessBaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店下套餐分组里的单品ID
     */
    @TableId(value = "commodity_store_single_id", type = IdType.ASSIGN_ID)

    private Long commodityStoreSingleId;

    /**
     * 门店下套餐分组 ID
     */

    private Long commodityStoreGroupId;

    private Long commodityStoreSpuId;



    /**
     * 门店下套餐里单品的顺序
     */
    private Integer commodityStoreSingleSort;

    /**
     * 门店下套餐里单品的加价
     */
    private BigDecimal commodityStoreSinglePrice;

    /**
     * 门店下套餐里单品的份数
     */
    private Integer commodityStoreSingleCopies;

    /**
     * 门店下套餐里单品状态
     */
    private Integer commodityStoreSingleStatus;


    private Long storeId;



    /**
     * 是这个子品在连锁商品库做单品时的主 id
     */
    private Long commodityId;

    /**
     * 划线价
     */
    private BigDecimal markingPrice;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品图片
     */
    private String commodityUrl;

    /**
     * 是否默认选中 0否 1是
     */
    private int defaultChoose;

    /**
     * 是否必选 0否 1是
     */
    private int requiredChoose;
    /**
     * 这个还在连锁商品时作为子品时候的所属的套餐 ID
     */
    private Long spuId;

    /**
     * 此品的规格名称
     */
    private String singleSkuName;
    /**
     * 此品的规格 id
     */
    private Long singleSkuId;

    /** 小程序上下架状态 1 上架 0 下架*/
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    private Integer storeStatus;

    /** 口味*/
    private String flavor;

}
