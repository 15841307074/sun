package com.htyoudao.youdao.module.promotion.dal.mysql.activity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityMapper extends BaseMapper<ActivityDO> {

    Page<ActivityDO> pageQuery(
        @Param("page") IPage<ActivityDO> page,
        @Param("ew") LambdaQueryWrapper<ActivityDO> queryWrapper,
        @Param("storeIds") List<Long> storeIds
    );

    List<ActivityDO> listQuery(
        @Param("ew") LambdaQueryWrapper<ActivityDO> queryWrapper,
        @Param("storeIds") List<Long> storeIds
    );

}
