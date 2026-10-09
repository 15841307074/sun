package com.htyoudao.youdao.module.promotion.service.activityCq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ActivityCqLogQueryService {

    @Resource
    private ActivityCqLogMapper activityCqLogMapper;

    @DS(DsNameConstants.SHARDING)
    public Page<ActivityCqLogDO> selectPage(Page<ActivityCqLogDO> pageParam, QueryWrapper<ActivityCqLogDO> queryWrapper) {
        return activityCqLogMapper.selectPage(pageParam, queryWrapper);
    }
}
