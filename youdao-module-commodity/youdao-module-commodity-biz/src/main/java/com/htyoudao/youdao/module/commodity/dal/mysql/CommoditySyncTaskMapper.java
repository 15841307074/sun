package com.htyoudao.youdao.module.commodity.dal.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.commodity.controller.admin.sync.VO.CommoditySyncListRespVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySyncTask;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommoditySyncTaskMapper extends BaseMapperX<CommoditySyncTask> {


    IPage<CommoditySyncListRespVO> syncPageList(IPage<?> page, @Param("ew") LambdaQueryWrapper<CommoditySyncTask> queryWrapper);

    default List<CommoditySyncTask> listByBatchNo(String batchNo) {
        return selectList(CommoditySyncTask::getBatchNo, batchNo);
    }
    @Delete("DELETE FROM commodity_sync_task WHERE create_time < #{createTime} LIMIT #{limit}")
    int deleteByCreateTimeLt(@Param("createTime") LocalDateTime createTime, @Param("limit") Integer limit);

}
