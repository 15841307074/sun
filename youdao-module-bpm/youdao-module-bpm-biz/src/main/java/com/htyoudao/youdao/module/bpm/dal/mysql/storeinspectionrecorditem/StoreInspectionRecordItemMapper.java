package com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionrecorditem;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.*;

/**
 * 巡店记录明细快照 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreInspectionRecordItemMapper extends BaseMapperX<StoreInspectionRecordItemDO> {

    default PageResult<StoreInspectionRecordItemDO> selectPage(StoreInspectionRecordItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreInspectionRecordItemDO>()
                .eqIfPresent(StoreInspectionRecordItemDO::getRecordId, reqVO.getRecordId())
                .eqIfPresent(StoreInspectionRecordItemDO::getChecklistId, reqVO.getChecklistId())
                .eqIfPresent(StoreInspectionRecordItemDO::getTitleSnap, reqVO.getTitleSnap())
                .eqIfPresent(StoreInspectionRecordItemDO::getPromptSnap, reqVO.getPromptSnap())
                .eqIfPresent(StoreInspectionRecordItemDO::getStandardSnap, reqVO.getStandardSnap())
                .eqIfPresent(StoreInspectionRecordItemDO::getMaxScoreSnap, reqVO.getMaxScoreSnap())
                .eqIfPresent(StoreInspectionRecordItemDO::getActualStatus, reqVO.getActualStatus())
                .eqIfPresent(StoreInspectionRecordItemDO::getActualScore, reqVO.getActualScore())
                .eqIfPresent(StoreInspectionRecordItemDO::getRewardAmount, reqVO.getRewardAmount())
                .eqIfPresent(StoreInspectionRecordItemDO::getActualComment, reqVO.getActualComment())
                .eqIfPresent(StoreInspectionRecordItemDO::getActualImages, reqVO.getActualImages())
                .eqIfPresent(StoreInspectionRecordItemDO::getIsRectified, reqVO.getIsRectified())
                .betweenIfPresent(StoreInspectionRecordItemDO::getRectifyTime, reqVO.getRectifyTime())
                .betweenIfPresent(StoreInspectionRecordItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StoreInspectionRecordItemDO::getCreateTime));
    }

}