package com.htyoudao.youdao.module.commodity.dal.mysql.invertory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportLogRecord;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterialLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImportLogRecordMapper extends BaseMapperX<ImportLogRecord> {
}
