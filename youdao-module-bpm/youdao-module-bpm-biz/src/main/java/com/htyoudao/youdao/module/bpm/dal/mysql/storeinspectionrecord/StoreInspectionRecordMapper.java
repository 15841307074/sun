package com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionrecord;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordPageReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 巡店记录主 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreInspectionRecordMapper extends BaseMapperX<StoreInspectionRecordDO> {

    default PageResult<StoreInspectionRecordDO> selectPage(StoreInspectionRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreInspectionRecordDO>()
                .inIfPresent(StoreInspectionRecordDO::getStoreId, reqVO.getStoreIds())
                .eqIfPresent(StoreInspectionRecordDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(StoreInspectionRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(StoreInspectionRecordDO::getStartTime, reqVO.getStartTime(),reqVO.getEndTime())
                .orderByDesc(StoreInspectionRecordDO::getStartTime));
    }

    default PageResult<StoreInspectionRecordDO> selectPage2(StoreInspectionRecordPageReqVO reqVO,
                                                           LambdaQueryWrapperX<StoreInspectionRecordDO> wrapper) {
        return BaseMapperX.super.selectPage(reqVO, wrapper);
    }

}