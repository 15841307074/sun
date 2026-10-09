package com.htyoudao.youdao.module.commodity.dal.mysql.triInventory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeMaterialDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeMaterialDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TriCommodityRecipeMaterialMapper extends BaseMapperX<TriRecipeMaterialDO> {

    @Delete("delete from tri_commodity_recipe_material where tri_id = #{triId} and replace_materials_id = #{materialsId}")
    void deleteSkuRecipeMaterial(Long triId, Long materialsId);
}
