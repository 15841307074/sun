package com.htyoudao.youdao.module.commodity.service.storeSku;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 门店下商品规格表 服务类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
public interface ICommodityStoreSkuService extends IService<CommodityStoreSku> {


    void deleteSpuByStoreId(Long storeId);

    void deleteAllStoreSpu();


    void deleteBySpuIds(List<Long> spuIds);

    List<CommodityStoreSku> selectBySpuId(Long commodityStoreSpuId);

    void deleteByLambda(LambdaQueryWrapper<CommodityStoreSku> eq);

    void saveBatch(List<CommodityStoreSku> commoditySkusList);

    List<CommodityStoreSku> selectBySpuIds(List<Long> commodityIds);

    void saveOrUpdateBatch(List<CommodityStoreSku> commodityStoreSkuList);

    void updateByDc(DCSpuUpdateReqVO reqVO);

    List<StoreSkuInfoDTO> selectSkuListForRpc(Set<Long> storeSkuIds);

    List<StoreSkuInfoDTO> selectSkuListByCommodityIdsForRpc(Long storeId, Set<Long> commodityIds);
}
