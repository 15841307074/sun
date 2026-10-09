package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeGroup;

import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreSingleRespVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle.CommodityStoreSingleSaveVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class CommodityStoreGroupSaveVO {


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


    private List<CommodityStoreSingleSaveVO> commodityStoreSingleRespVOList = new ArrayList<>();
}
