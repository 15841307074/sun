package com.htyoudao.youdao.module.commodity.service.sync.dto;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreCategory;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySyncCategoryDTO {

    private CommodityStoreCategory category;

    List<CommoditySyncSpuDTO> spuList;
}
