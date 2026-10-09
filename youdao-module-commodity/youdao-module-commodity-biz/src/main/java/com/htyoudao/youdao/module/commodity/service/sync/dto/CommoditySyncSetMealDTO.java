package com.htyoudao.youdao.module.commodity.service.sync.dto;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import java.util.List;
import lombok.Data;

@Data
public class CommoditySyncSetMealDTO {
    //转换套餐分组对象
    CommodityStoreGroup storeGroup;
    //转换套餐单品对象
    List<CommodityStoreSingle> storeSingles;
}
