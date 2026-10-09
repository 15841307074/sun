package com.htyoudao.youdao.module.commodity.service.storeSpu.strategy;

import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import java.util.List;

public interface CommodityClientStrategy {
    List<StoreCategoryDTO> filter(List<StoreCategoryDTO> products);
}
