package com.htyoudao.youdao.module.system.service.wxtemplate;


import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateDetailReqVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplatePageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.wxtemplate.vo.StoreWecomTemplateReqVO;

import java.util.List;

public interface StoreWecomTemplateService {


    Integer insertstoreWecomTemplate(StoreWecomTemplateReqVO storeWecomTemplateReqVO);

    Integer remove(long id);

    Integer updatestoreWecomTemplate(StoreWecomTemplateReqVO storeWecomTemplateReqVO);

    StoreWecomTemplateDetailReqVO getByDetail(long id);

    List<StoreWecomTemplatePageRespVO> getList();

    Integer isRelease(Long id, Integer status);

    String getStoreIdByTemplate(Long storeId);
}
