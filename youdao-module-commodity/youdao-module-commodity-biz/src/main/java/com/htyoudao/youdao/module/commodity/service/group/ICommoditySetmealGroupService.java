package com.htyoudao.youdao.module.commodity.service.group;


import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySetmealGroup;

import java.util.List;


public interface ICommoditySetmealGroupService  {


    void save(CommoditySetmealGroup commoditySetmealGroup);

    void deleteBySpuId(Long commodityId);

    List<CommoditySetmealGroup> selectBySpuId(Long commodityId);

    List<CommoditySetmealGroup> selectBySpuIds(List<Long> commodityIds);

    void deleteBySpuIds(List<Long> commodityIds);
}
