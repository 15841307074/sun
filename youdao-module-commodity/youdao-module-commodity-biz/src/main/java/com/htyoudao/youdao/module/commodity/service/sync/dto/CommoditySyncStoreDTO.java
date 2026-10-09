package com.htyoudao.youdao.module.commodity.service.sync.dto;

import java.util.List;
import lombok.Data;

@Data
public class CommoditySyncStoreDTO {

    List<CommoditySyncCategoryDTO> categories;

    private Long storeId;
}
