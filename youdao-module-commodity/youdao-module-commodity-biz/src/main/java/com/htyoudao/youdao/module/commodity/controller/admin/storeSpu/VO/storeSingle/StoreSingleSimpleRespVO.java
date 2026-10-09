package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreSingleSimpleRespVO {

    /**
     * 门店下商品的唯一ID
     */
    @Schema(description = "门店下商品的唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;

    /**
     * 门店下商品名称
     */
    @Schema(description = "门店下商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSpuName;



    /**
     * 规格
     */
    @Schema(description = "规格", requiredMode = Schema.RequiredMode.REQUIRED)
    List<StoreSingleSkuVO> storeSingleSkuVOS = new ArrayList<>();
}
