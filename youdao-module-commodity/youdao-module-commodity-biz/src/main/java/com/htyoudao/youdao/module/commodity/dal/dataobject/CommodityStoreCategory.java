package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 门店下商品分类表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Getter
@Setter
@TableName("commodity_store_category")
@EqualsAndHashCode(callSuper = true)
public class CommodityStoreCategory extends TimeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店下商品分类ID
     */
    @TableId(value = "commodity_store_category_id", type = IdType.ASSIGN_ID)
    private Long commodityStoreCategoryId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 类型  是否下单必选分组 1 是 0否
     */
    private Integer type;

    /**
     * 门店商品分类URL
     */
    private String commodityStoreCategoryImage;

    /**
     * 套餐原始 ID
     */
    private Long commodityStorePrimitiveCategoryId;

    /**
     * 门店商品分类名称
     */
    private String commodityStoreCategoryName;

    /**
     * 门店商品分类状态
     */
    private Integer commodityStoreCategoryStatus;

    /**
     * 门店下商品是否锁
     */
    private Boolean commodityStoreCategoryLock;

    /**
     * 门店下商品分类的顺序
     */
    private Integer commodityStoreCategorySort ;




}
