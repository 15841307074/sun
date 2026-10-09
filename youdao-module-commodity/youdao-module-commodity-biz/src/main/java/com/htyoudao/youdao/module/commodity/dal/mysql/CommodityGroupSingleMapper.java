package com.htyoudao.youdao.module.commodity.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityGroupSingle;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommodityGroupSingleMapper extends BaseMapperX<CommodityGroupSingle> {

    default List<CommodityGroupSingle> listByGroupId(Long spuId, Long groupId) {
        LambdaQueryWrapper<CommodityGroupSingle> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CommodityGroupSingle::getSpuId, spuId);
        queryWrapper.eq(CommodityGroupSingle::getGroupId, groupId);
        return selectList(queryWrapper);
    }
}
