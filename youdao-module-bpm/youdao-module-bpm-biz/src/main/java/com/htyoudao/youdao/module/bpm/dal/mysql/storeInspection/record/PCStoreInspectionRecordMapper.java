package com.htyoudao.youdao.module.bpm.dal.mysql.storeInspection.record;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionTypeDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点检项 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface PCStoreInspectionRecordMapper extends BaseMapperX<StoreInspectionRecordDO> {
}