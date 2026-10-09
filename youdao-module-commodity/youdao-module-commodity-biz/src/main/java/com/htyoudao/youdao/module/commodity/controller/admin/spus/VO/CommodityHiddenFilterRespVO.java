package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "商品隐藏状态过滤 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityHiddenFilterRespVO {

    @Schema(description = "过滤后的商品ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> commodityIds = new ArrayList<>();

    @Schema(description = "返回商品ID是否为隐藏商品 1是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isHidden;
}
