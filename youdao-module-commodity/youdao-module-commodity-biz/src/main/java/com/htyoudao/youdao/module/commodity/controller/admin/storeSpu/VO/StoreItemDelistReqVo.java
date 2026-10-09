package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 门店下单品下架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemDelistReqVo {

    @Schema(description = "门店下的商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityStoreSpuId;


    @Schema(description = "1 wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不能为空")
    private Integer chooseView;

    @Schema(description = "门店 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店ID不能为空")
    private Long storeId;


    @Schema(description = "门店需要下架的商品")
    private List<CommodityStoreSpuRespVo> storeSpus = new ArrayList<>();
    //true 点餐机 false pc 为获取操作端
    private Boolean isApp;

}
