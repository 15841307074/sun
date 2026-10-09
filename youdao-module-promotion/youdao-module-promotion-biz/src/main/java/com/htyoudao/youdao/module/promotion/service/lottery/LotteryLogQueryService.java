package com.htyoudao.youdao.module.promotion.service.lottery;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class LotteryLogQueryService {

    @Resource
    private LotteryLogMapper lotteryLogMapper;
    @DS(DsNameConstants.SHARDING)
    public Page<LotteryLogDO> selectPage(Page<LotteryLogDO> pageParam, LambdaQueryWrapper<LotteryLogDO> queryWrapper) {
        return lotteryLogMapper.selectPage(pageParam, queryWrapper);
    }
}
