package com.htyoudao.youdao.module.system.service.wxstore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.wxstore.vo.*;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreImgRespVO;
import com.htyoudao.youdao.module.system.controller.app.wxstore.vo.AppWeChatStoreRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomImgDO;
import org.springframework.web.bind.annotation.RequestParam;

public interface StoreWecomConfigService {

    /**
     * 企业微信查询门店配置信息
     *
     * @param id 主键
     * @return StoreWecomConfigRespVO
     */
    public StoreWecomConfigRespVO selectStoreWecomConfigById(Long id);

    Integer insertStoreWecomConfig(StoreWecomConfigReqVO storeWecomConfig);

    Integer deleteStoreWecomConfig(Long storeWecomConfig);

    PageResult<StoreWecomConfigRespVO> getStoreWecomPage(StoreWecomPageReqVO storeWecomPageReq);

    AppWeChatStoreRespVO getQrCodeByStoreId(Long storeId);

    void updateCodeType(StoresCodeUpdateVO storeUpdateVO);

    Integer communityImgAdd(StoreWecomImgReqVO storeWecomImgReqVO);

    AppWeChatStoreImgRespVO getQrCodeBgByStoreId(Long storeId, Long couponId, Long activityId, Long packageId);

    StoreWecomImgDO communityImgDetail();
}
