package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp;

import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreSpuRespVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreSingleUpRespVO {

    @Schema(description = "门店下的商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;

    @Schema(description = "1 wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;

    @Schema(description = "门店 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "达成上架条件的套餐", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CommodityStoreSpuRespVo> storeSpu= new ArrayList<>();
}
