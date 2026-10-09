package com.htyoudao.youdao.module.commodity.dal.mysql.invertory;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.ImportTask;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImportTaskMapper extends BaseMapperX<ImportTask> {
    
    @Select("SELECT * FROM import_task ORDER BY create_time DESC")
    List<ImportTask> selectAllOrderByCreateTime();
}
