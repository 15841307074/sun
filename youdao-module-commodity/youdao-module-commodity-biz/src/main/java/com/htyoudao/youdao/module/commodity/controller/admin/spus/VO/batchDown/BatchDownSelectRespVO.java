package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.batchDown;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusQueryReqVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchDownSelectRespVO {

    @Schema(description = "1wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不能为空")
    private Integer chooseView;

    @Schema(description = "单品 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> singleIds = new ArrayList<>();

    @Schema(description = "套餐 ID 集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> packageIds = new ArrayList<>();

    @Schema(description = "还需下架的商品", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CommoditySpusQueryReqVo> commoditySpusQueryReqVos = new ArrayList<>();

}
