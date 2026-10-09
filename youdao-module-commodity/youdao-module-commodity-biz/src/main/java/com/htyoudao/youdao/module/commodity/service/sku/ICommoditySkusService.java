package com.htyoudao.youdao.module.commodity.service.sku;


import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;

import java.util.List;
import java.util.Map;

public interface ICommoditySkusService {


    Map<String, Object> saveBatch(List<CommoditySkus> commoditySkusList);

    void deleteBySpuId(Long commodityId);

    List<CommoditySkus> selectBySpuIds(List<Long> commodityIds);


    List<CommoditySkus> selectBySpuId(Long id);

    Long selectSkuIdCountByCommodityId(Long commodityId);
}
