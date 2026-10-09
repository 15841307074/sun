package com.htyoudao.youdao.module.bpm.dal.mysql.storeinspectionitemlog;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo.*;

/**
 * 巡店项操作变更日志 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface StoreInspectionItemLogMapper extends BaseMapperX<StoreInspectionItemLogDO> {

    default PageResult<StoreInspectionItemLogDO> selectPage(StoreInspectionItemLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreInspectionItemLogDO>()
                .eqIfPresent(StoreInspectionItemLogDO::getRecordId, reqVO.getRecordId())
                .orderByDesc(StoreInspectionItemLogDO::getCreateTime));
    }

}