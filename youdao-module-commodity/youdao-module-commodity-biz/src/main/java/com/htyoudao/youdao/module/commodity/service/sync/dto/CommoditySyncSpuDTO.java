package com.htyoudao.youdao.module.commodity.service.sync.dto;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import java.util.List;
import lombok.Data;

@Data
public class CommoditySyncSpuDTO {

    private CommodityStoreSpu spu;
    //转换sku对象
    List<CommodityStoreSku> storeSkuList;

    //转换套餐对象
    List<CommoditySyncSetMealDTO> setMealList;

    List<CommodityTag> commodityTags;

}
