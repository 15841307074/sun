package com.htyoudao.youdao.module.bpm.service.storeInspection.record;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record.vo.StoreInspectionRecordQueryReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;

/**
 * 点检项 Service 接口
 *
 * @author 超级管理员
 */
public interface PCStoreInspectionRecordService {

    PageResult<StoreInspectionRecordDO> getStoreInspectionRecordPage(StoreInspectionRecordQueryReqVO pageReqVO);
}