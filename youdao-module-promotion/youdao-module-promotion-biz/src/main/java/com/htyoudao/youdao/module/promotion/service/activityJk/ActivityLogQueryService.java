package com.htyoudao.youdao.module.promotion.service.activityJk;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityCardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityCardLog.ActivityCardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogQueryService {

    @Resource
    private ActivityCardLogMapper activityCardLogMapper;
    @DS(DsNameConstants.SHARDING)
    public Page<ActivityCardLogDO> selectPage(Page<ActivityCardLogDO> pageParam, LambdaQueryWrapper<ActivityCardLogDO> queryWrapper) {
        return activityCardLogMapper.selectPage(pageParam, queryWrapper);
    }
}
