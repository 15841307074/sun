package com.htyoudao.youdao.module.commodity.service.inventory;


import com.htyoudao.youdao.module.commodity.controller.admin.recipe.VO.*;

import java.util.List;

public interface ICommodityRecipeService {

    List<RecipeCommoditySkuRespVO> selectByCommodityId(Long commodityId);

    List<RecipeCommoditySkuRespVO> selectRelatedByCommodityId(Long commodityId);

    void truncRecipeMaterial(Long commodityId, Long skuId, Long materialsId);

    void insertRecipe(List<RecipeReqVO> recipeReqVOList,Long commodityId);

    void batchInsert(List<RecipeMaterialSelectReqVO> recipeMaterialSelectReqVOList);

    void saveUnSelectedRecipeMaterial(List<RecipeMaterialUnSelectedReqVO> recipeMaterialUnSelectedReqVOList);

    List<RecipeMaterialUnSelectedRespVO> getUnSelectedRecipeMaterials(Long commodityId, Long skuId, Long replaceMaterialsId);
}
