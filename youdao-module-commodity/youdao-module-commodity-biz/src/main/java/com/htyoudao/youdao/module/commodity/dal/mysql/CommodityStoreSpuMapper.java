package com.htyoudao.youdao.module.commodity.dal.mysql;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSpuCountDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.units.qual.C;

@Mapper
public interface CommodityStoreSpuMapper extends BaseMapperX<CommodityStoreSpu> {


    default List<CommodityStoreSpu> selectListByStoreId(Long storeId){
        return selectList(CommodityStoreSpu::getStoreId, storeId);
    }

    @DataPermission(enable = false) // 不开启数据权限 异步调用场景会出现问题
    default List<CommodityStoreSpu> listByCommodityId(Long storeId, Long commodityId){
        LambdaQueryWrapper<CommodityStoreSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommodityStoreSpu::getStoreId, storeId);
        queryWrapper.eq(CommodityStoreSpu::getCommodityId, commodityId);
        return selectList(queryWrapper);
    }

    List<StoreSpuCountDTO> saleStoreCountByCommodityIds(
        @Param("commodityIds") Set<Long> commodityIds,
        @Param("storeIds") Set<Long> storeIds
        );

}
