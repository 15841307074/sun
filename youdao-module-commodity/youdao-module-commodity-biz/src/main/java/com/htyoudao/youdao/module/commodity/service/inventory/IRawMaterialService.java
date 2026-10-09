package com.htyoudao.youdao.module.commodity.service.inventory;

import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface IRawMaterialService {



    /**
     * 查询门店原材料列表
     * @param reqVO
     * @return
     */
    RawMaterialRespVO queryMaterialList(@Valid SelectMaterialListReqVO reqVO);


    RawMaterialTotalRespVO caleTotalVo(List<RawMaterialDetailVO> details);

    /**
     * 更改原材料状态
     * @param id
     * @param isEnable
     * @return
     */
    Boolean modifyStatus(Long id, Integer isEnable);

    /**
     * 删除原材料
     * @param id
     * @return
     */
    Boolean removeById(Long id);

    RawMaterial selectMaterial(@NotNull(message = "门店 ID 不能为空") Long storeId, @NotNull(message = "商品编号 不能为空") String commodityCode);

    void saveMaterial(SaveMaterialReqVO reqVo);

    void updateMaterial(SaveMaterialReqVO reqVo);

    void batchSaveOrUpdate(List<SaveMaterialReqVO> saveMaterialReqVOS);
}
