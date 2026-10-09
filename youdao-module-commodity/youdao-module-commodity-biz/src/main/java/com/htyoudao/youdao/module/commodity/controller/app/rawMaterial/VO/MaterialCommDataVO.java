package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreRespVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MaterialCommDataVO {

    @Schema(description = "商品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityStoreRespVo> commodityStoreRespVos;

    @Schema(description = "原材料列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RawMaterial> materials;
}
