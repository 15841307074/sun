package com.htyoudao.youdao.module.commodity.dal.mysql.invertory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RawMaterialMapper extends BaseMapperX<RawMaterial> {

    default RawMaterial selectMaterial(Long storeId, String commodityCode){
        LambdaQueryWrapperX<RawMaterial> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(RawMaterial::getStoreId, storeId);
        queryWrapperX.eq(RawMaterial::getCommodityCode, commodityCode);
        queryWrapperX.last(" limit 1");
        return this.selectOne(queryWrapperX);
    }

}
