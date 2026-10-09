package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

/** 查询前先选择抽奖日志分库。 */
@Service("lotteryWinnerQuery")
@DS("sharding")
public class LotteryWinnerQueryImpl implements LotteryWinnerQuery {
    @Resource
    private LotteryLogMapper logs;

    @Override
    public List<LotteryLogDO> latest(long businessId, long activityId, long settingsId) {
        return logs.selectList(new LambdaQueryWrapper<LotteryLogDO>()
                .select(LotteryLogDO::getId, LotteryLogDO::getCreateTime, LotteryLogDO::getMemberMobile,
                        LotteryLogDO::getPrizeName, LotteryLogDO::getIsGuarantees, LotteryLogDO::getPrizeType)
                .eq(LotteryLogDO::getBusinessId, businessId)
                .in(LotteryLogDO::getLotteryId, List.of(activityId, settingsId).stream().distinct().toList())
                .eq(LotteryLogDO::getIsGuarantees, 0)
                .ne(LotteryLogDO::getPrizeType, 4)
                .orderByDesc(LotteryLogDO::getCreateTime, LotteryLogDO::getId).last("LIMIT 20"));
    }
}
