package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class CommodityStoreGroupRespVO {


    private static final long serialVersionUID = 1L;

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

    private Integer chooseMany;


    private List<CommodityStoreSingleRespVO> commodityStoreSingleRespVOList = new ArrayList<>();
}
