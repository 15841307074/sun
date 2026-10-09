package com.htyoudao.youdao.module.commodity.service.materialLoss;


import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;

import java.util.List;

/**
 * 商品拆分原材料Service
 */
public interface RawMaterialInventoryService {

    /**
     *
     * @param commoditySplitMaterialReqVO
     * @return
     */
    MaterialOwnDataRespVo splitCommodity(CommoditySplitMaterialReqVO commoditySplitMaterialReqVO);
}
