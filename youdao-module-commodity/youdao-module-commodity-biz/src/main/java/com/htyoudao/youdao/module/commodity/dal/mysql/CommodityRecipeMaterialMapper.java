package com.htyoudao.youdao.module.commodity.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeMaterialDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommodityRecipeMaterialMapper extends BaseMapperX<RecipeMaterialDO> {

    @Delete("delete from commodity_recipe_material where commodity_id = #{commodityId} and sku_id = #{skuId} and replace_materials_id = #{materialsId}")
    void deleteSkuRecipeMaterial(Long commodityId, Long skuId, Long materialsId);
}
