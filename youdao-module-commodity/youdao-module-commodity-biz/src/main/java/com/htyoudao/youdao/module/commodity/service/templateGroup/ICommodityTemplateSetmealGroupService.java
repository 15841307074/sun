package com.htyoudao.youdao.module.commodity.service.templateGroup;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSetmealGroup;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import java.util.List;

public interface ICommodityTemplateSetmealGroupService {
    void saveBatch(List<CommodityTemplateSetmealGroup> addCommodityTemplateSetmealGroupList);

    void deleteByTemplateSpuIds(List<Long> commodityTemplateIds);

    List<CommodityTemplateSetmealGroup> selectByTemplateSpuIds(List<Long> templateCommodityIds);

    List<CommodityTemplateSetmealGroup> selectByTemplateId(Long commodityTemplateId);

}
