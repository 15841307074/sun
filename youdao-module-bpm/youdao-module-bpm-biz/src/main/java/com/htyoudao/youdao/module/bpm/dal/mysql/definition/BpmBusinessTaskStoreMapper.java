package com.htyoudao.youdao.module.bpm.dal.mysql.definition;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BussinessTaskStoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BpmBusinessTaskStoreMapper extends BaseMapperX<BussinessTaskStoreDO> {
}
