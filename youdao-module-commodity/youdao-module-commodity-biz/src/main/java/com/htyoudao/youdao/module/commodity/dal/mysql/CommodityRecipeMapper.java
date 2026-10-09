package com.htyoudao.youdao.module.commodity.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommodityRecipeMapper extends BaseMapperX<RecipeDO> {
    @Delete("delete from commodity_sku_recipe where commodity_id = #{commodityId}")
    void deleteRecipe(Long commodityId);

    @Delete("delete from commodity_sku_recipe where sku_Id = #{skuId} and commodity_id = #{commodityId}")
    void deleteSkuRecipe(Long skuId, Long commodityId);
}
