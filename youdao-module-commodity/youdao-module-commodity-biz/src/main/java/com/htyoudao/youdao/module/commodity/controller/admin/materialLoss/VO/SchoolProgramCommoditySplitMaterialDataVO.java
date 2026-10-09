package com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO;

import com.htyoudao.youdao.module.commodity.api.VO.MaterialDataVo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品拆分原材料VO
 */
@Data
public class SchoolProgramCommoditySplitMaterialDataVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3433100638234392632L;

    //必传
    private Long storeId;

    private String storeName;
    private String name;


    private List<MaterialDataVo> materialDataVos = new ArrayList<>();
}
