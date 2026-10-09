package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.batchDown;

import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreSpuRespVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreBatchDownSelectRespVO {

    @Schema(description = "1wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;

    @Schema(description = "单品 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> singleIds = new ArrayList<>();

    @Schema(description = "套餐 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> packageIds = new ArrayList<>();

    @Schema(description = "还需下架条件的套餐", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CommodityStoreSpuRespVo> storeSpu= new ArrayList<>();

    @Schema(description = "门店 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    Long storeId;

}
