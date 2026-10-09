package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreSingleUpConfirmReqVO {

    @Schema(description = "门店下的商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;

    @Schema(description = "1 wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;


    @Schema(description = "门店 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "勾选的套餐 IDs", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> packageIds = new ArrayList<>();

    //true 点餐机 false pc 为获取操作端
    private Boolean isApp;

}
