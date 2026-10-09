
package com.htyoudao.youdao.module.commodity.dal.mysql.invertory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialStocktake;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RawMaterialStocktakeMapper extends BaseMapperX<RawMaterialStocktake> {

    @Select("SELECT MAX(create_time) FROM raw_material_stocktake WHERE store_id = #{storeId}")
    Date selectLastTakeTime(@Param("storeId") Long storeId);

}
