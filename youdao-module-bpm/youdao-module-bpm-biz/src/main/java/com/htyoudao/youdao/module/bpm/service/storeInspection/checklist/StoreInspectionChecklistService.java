package com.htyoudao.youdao.module.bpm.service.storeInspection.checklist;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionChecklistDO;
import jakarta.validation.*;

import java.util.List;

/**
 * 点检项 Service 接口
 *
 * @author 超级管理员
 */
public interface StoreInspectionChecklistService {

    Integer createType(StoreInspectionTypeReqVO storeInspectionTypeReqVO);

    List<StoreInspectionTypeRespVO> getAllType(String typeName);

    Integer deleteTypeById(Long typeId);

    Integer updateType(StoreInspectionTypeReqVO storeInspectionTypeReqVO);

    Integer create(CheckListReqVO checkListReqVO);

    Integer deleteChecklistById(Long checklistId);

    Integer update(CheckListReqVO checkListReqVO);

    List<CheckListRespVO> getChecklistsByTypeId(Long typeId);

    PageResult<CheckListRespVO> getAllChecklists(CheckListsQueryReqVO checkListsQueryReqVO);

    Integer sortChecklists(List<CheckListsSortReqVO> list);
}