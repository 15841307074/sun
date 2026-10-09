package com.htyoudao.youdao.module.commodity.service.storeSingle;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.CommodityStoreUpdateReqVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSingle;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;

import java.util.List;
import java.util.Set;


/**
 * <p>
 * 门店下商品套餐分组里的单品表 服务类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
public interface ICommodityStoreSingleService  extends IService<CommodityStoreSingle> {


    void deleteSpuByStoreId(Long storeId);

    void deleteAllStoreSpu();

    void deleteBySpuIds(List<Long> spuIds);

    List<CommodityStoreSingle> selectByChooseViewAndSpuId(Long commodityId, Integer chooseView, Long storeId);

    List<CommodityStoreSingle> selectByGroupIdsAndStoreId(List<Long> groupIds, Long storeId);


    List<Long> updateDownStatusBySpuIdAndChooseView(Long storeId, Long commodityId, Integer chooseView);

    List<Long> updateUpStatusBySpuId(Long storeId, Long commodityId, Integer chooseView);

    CommodityStoreSingle getOne(Long commodityStoreSingleId);

    void updateWxStatusBySingleId(Long commodityStoreSingleId, Integer wxStatus);

    void updateStoreStatusBySingleId(Long commodityStoreSingleId, Integer storeStatus);

    List<CommodityStoreSingle> selectByGroupId(Long commodityStoreGroupId);

    void deleteByLambda(LambdaQueryWrapper<CommodityStoreSingle> in);

    List<CommodityStoreSingle> selectByGroupIds(List<Long> groupIds);

    List<CommodityStoreSingle> selectBySpuIds(List<Long> spuIds);

    void deletByGroupIds(List<Long> groups);

    void saveBatch(List<CommodityStoreSingle> singleList);

    List<StoreSingleInfoDTO> selectSingleListForRpc(Set<Long> singleIds);

    List<Long> updateNameBySingleSpu(CommodityStoreUpdateReqVo commodityStoreSpu);


    List<CommodityStoreSingle> selectSpuIds(List<Long> commodityId, Long storeId);

    List<Long> updateUpStatusBySpuIdNew(Long storeId, List<Long> commodityStorePrimitiveSpuId, Integer chooseView,Integer upOrDown);

    List<CommodityStoreSingle> selectByStoreSpuIds(List<Long> packageIDs);

    void emitSyncToSubProducts(String flavorJson, Long commodityId, Long storeId);


    List<Long> uodateSingleFlavor(String flavorJson, Long commodityStorePrimitiveSpuId, Long storeId);
}
