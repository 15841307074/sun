package com.htyoudao.youdao.module.commodity.dal.mysql;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreSkuDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


/**
 * 门店下商品规格Mapper接口
 *
 * @author Qizhongnan
 * @date 2024-05-25
 */
@Mapper
public interface CommodityStoreSkuMapper extends BaseMapperX<CommodityStoreSku>
{
    /**
     * 查询门店下商品规格列表
     *
     * @param storeSkuIds 门店下商品规格ID集合
     * @return 门店下商品规格列表
     */
    List<StoreSkuDTO> selectSkuListForRpc(@Param("storeSkuIds") Set<Long> storeSkuIds);

    List<StoreSkuDTO> selectSkuListByCommodityIdsForRpc(@Param("storeId") Long storeId,
                                                         @Param("commodityIds") Set<Long> commodityIds);
}
