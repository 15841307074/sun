package com.htyoudao.youdao.module.member.dal.mysql.pointsLog;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsImportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PointsImportTaskMapper extends BaseMapperX<PointsImportTask> {
    
    @Select("SELECT * FROM points_import_task ORDER BY create_time DESC")
    List<PointsImportTask> selectAllOrderByCreateTime();
}
