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
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 门店下商品的套餐分组表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Getter
@Setter
@TableName("commodity_store_group")
@EqualsAndHashCode(callSuper = true)
public class CommodityStoreGroup extends BusinessBaseDO implements Serializable {



    /**
     * 门店下套餐分组的ID
     */
    @TableId(value = "commodity_store_group_id", type = IdType.ASSIGN_ID)
    private Long commodityStoreGroupId;

    private Long storeId;


    /**
     * 门店下商品的ID
     */
    private Long commodityStoreSpuId;

    private String commodityStoreGroupName;

    /**
     * 门店下套餐分组的状态
     */
    private Integer commodityStoreGroupStatus;

    /**
     * 门店下套餐分组的排序
     */
    private Integer commodityStoreGroupSort;

    /**
     * 门店下套餐分组里可选的数量
     */
    private Integer commodityStoreGroupChoose;

    /**
     * 门店下套餐分组里分组属性（1可选 2固定 3加价组）
     */
    private Integer commodityStoreGroupAttribute;
    /**
     * 同一商品是否可选多份 1 是 0否
     */
    private Integer chooseMany;

    //套餐内的单品
    @TableField(exist = false)
    List<CommodityStoreSingle> commodityStoreSingleList = new ArrayList<>();


}
