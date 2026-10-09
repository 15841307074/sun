package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 单品下架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySingleDownReqVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;


    @Schema(description = "wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不能为空")
    private Integer chooseView;

    @Schema(description = "需要下架的商品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CommoditySpus> spusList = new ArrayList<>();
}
