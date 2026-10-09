package com.htyoudao.youdao.module.commodity.service.triInventory;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.admin.triRecipe.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriFormulationDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeDO;

import java.util.List;

public interface ITriRecipeService {

    List<TriRecipeDO> selectById(Long id);

    List<TriRecipeDO> selectByNames(TriRecipeReqVO recipeReqVO);

    void truncRecipeMaterial(Long triId, Long materialsId);

    void insertRecipe(List<TriFormulationRecipeReqVO> recipeReqVOList,Long triId);

    void saveUnSelectedRecipeMaterial(List<TriRecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList);

    List<TriRecipeMaterialUnSelectedRespVO> getUnSelectedRecipeMaterials(Long triId, Long replaceMaterialsId);


    PageResult<TriFormulationDO> selectTriFormulationList(TriFormulationReqVO recipeReqVO);

    void delTriFormulationList(Long id);

    void batchInsert(List<TriRecipeMaterialSelectReqVO> selectReqVOS);

    TriFormulationDO insertTriFormulation(TriRecipeInsertOrUpdateReqVO triRecipeReqVO);

    void delTriFormulation(Long triId);

    PageResult<TriFormulationRelatedRespVO> selectTriFormulationRelatedList(TriFormulationReqVO recipeReqVO);
}
