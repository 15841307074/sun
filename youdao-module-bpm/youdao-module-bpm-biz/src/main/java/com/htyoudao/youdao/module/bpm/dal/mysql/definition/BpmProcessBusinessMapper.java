package com.htyoudao.youdao.module.bpm.dal.mysql.definition;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.business.BpmBusinessQueryDO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BpmProcessBusinessMapper extends BaseMapperX<BpmBusinessDO> {



    @Update({
        "<script>",
        "UPDATE business_task SET oa_project_id = NULL WHERE task_id IN",
        "<foreach collection='businessTaskIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    void removeOABusiness(@Param("businessTaskIds") List<Long> businessTaskIds);
}
