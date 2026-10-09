package com.htyoudao.youdao.module.commodity.dal.mysql.triInventory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriFormulationDO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TriRecipeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TriFormulationMapper extends BaseMapperX<TriFormulationDO> {
}
