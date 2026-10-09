package com.htyoudao.youdao.module.bpm.dal.mysql.definition;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessCopyDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BpmBusinessCopyMapper extends BaseMapperX<BpmBusinessCopyDO> {
    void batchInsertCopyFlag(@Param("list") List<BpmBusinessCopyDO> bpmProcessInstanceQueryReqVO,
                       @Param("id") Long id);
}
