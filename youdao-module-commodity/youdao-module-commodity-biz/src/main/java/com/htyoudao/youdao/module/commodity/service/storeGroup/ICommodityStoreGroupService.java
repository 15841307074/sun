package com.htyoudao.youdao.module.commodity.service.storeGroup;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;

import java.util.List;

/**
 * <p>
 * 门店下商品的套餐分组表 服务类
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
public interface ICommodityStoreGroupService  extends IService<CommodityStoreGroup> {

    void deleteSpuByStoreId(Long storeId);

    void deleteAllStoreSpu();


    void deletBySpuIds(List<Long> spuIds);

    List<CommodityStoreGroup> selectByGroupIdsAndStoreId(List<Long> groupIds, Long storeId);

    List<CommodityStoreGroup> selectBySpuId(Long storeId, Long commodityStoreSpuId);

    CommodityStoreGroup getOne(Long commodityStoreGroupId);


    void deleteByLambda(LambdaQueryWrapper<CommodityStoreGroup> eq);

    List<CommodityStoreGroup> selectBySpuIds(List<Long> spuIds);

    void updateByDc(DCSpuUpdateReqVO reqVO);

    void delbySpuId(Long commodityStoreSpuId);
}
