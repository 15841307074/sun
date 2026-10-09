package com.htyoudao.youdao.module.system.service.applet;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppAppletPageManagementReqVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementFullStoreRespVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementReqVO;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.AppletPageManagementRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementDO;

import java.util.List;

public interface AppletPageManagementService {
    PageResult<AppletPageManagementFullStoreRespVO> getPage(AppletPageManagementReqVO appletPageManagement);

    void createAppletPage(AppletPageManagementReqVO appletPageManagement);

    void updateAppletPageStatus(AppletPageManagementReqVO appletPageManagement);

    AppletPageManagementRespVO getInfo(Long appletPageId);

    void deleteAppletPage(Long appletPageId);

    void updateAppletPage(AppletPageManagementReqVO appletPageManagement);

    Object getAppletPageManagementForApplet(AppletPageManagementReqVO appletPageManagement);

    void flushAppletPage();

    String getAppletPageManagementForAppletNew(AppAppletPageManagementReqVO appletPageManagement);

    void addRedisByStoreTag(Long storeId, Long tagId, Long businessId);

    void delRedisByStoreTag(Long storeId, Long tagId, Long businessId);

    void addRedisByStoreTags(Long storeId, List<Long> tagIdList, Long businessId);

    void delRedisByStoreTags(Long storeId, List<Long> tagIdList, Long businessId);

    void delAppletPageManagementTags(List<Long> tagIdList);

    boolean appletPageExistByStoreTagGroup(long tagGroupId, long businessId);
}
