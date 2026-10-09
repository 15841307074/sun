package com.htyoudao.youdao.module.promotion.service.advertising;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.*;
import com.htyoudao.youdao.module.promotion.controller.app.advertising.VO.AdvertisingConfigReqVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.AdvertisingDO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import jakarta.validation.Valid;


import java.util.List;
import java.util.Map;

public interface AdvertisingService extends IService<AdvertisingDO> {
    PageResult<AdvertisingPageRespVO> getPage(AdvertisingPageReqVO advertising);

    AdvertisingRespVO getInfo(Long id);

    void createAdvertising(@Valid AdvertisingSaveReqVO saveReqVO);

    void updateAdvertising(@Valid AdvertisingSaveReqVO updateVo);

    void deleteAdvertising(Long id);

    void updateAdvertisingStatus(AdvertisingStatusReqVO advertisingStatusReqVO);

    List<AdvertisingConfigReqVo>  appletGetAdvertisingNew(@Valid AdvertisingVO advertisingVO);


    PageResult<StorePageResVO> selectByStoreList(AdvertisingStorePageReqVO storePageReqVO);

    List<StoreInfoDTO> selectCheckedStoreList(AdvertisingStorePageReqVO storePageReqVO);

    void execAdvertising();

    void deleteByStoreId(Long advertisingId, Long storeId);

    void deleteRedis();

    Boolean advertisingDataConversion();
}
