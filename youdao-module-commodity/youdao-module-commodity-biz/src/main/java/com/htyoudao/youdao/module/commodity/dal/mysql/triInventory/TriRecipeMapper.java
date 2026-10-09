package com.htyoudao.youdao.module.commodity.dal.mysql.triInventory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.RecipeDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TriRecipeMapper extends BaseMapperX<TriRecipeDO> {
    @Delete("delete from tri_commodity_recipe where tri_id = #{triId}")
    void deleteRecipe(Long triId);
}
