package com.htyoudao.youdao.module.promotion.service.activityAnswerApp;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRewardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 有奖问答共用奖品库存短事务服务。
 */
@Service
@DS(DsNameConstants.SHARDING)
public class ActivityAnswerRewardStockService {

    @Resource
    private ActivityAnswerRewardMapper rewardMapper;

    /**
     * 原子预占一份共用库存，并立即提交释放库存行锁。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean reserve(Long rewardId) {
        return rewardMapper.increaseUsedNum(rewardId) > 0;
    }

    /**
     * 发奖失败时归还一份共用库存。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void rollback(Long rewardId) {
        rewardMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRewardDO>()
                .eq(ActivityAnswerRewardDO::getId, rewardId)
                .setSql("used_num = CASE WHEN COALESCE(used_num, 0) > 0 THEN COALESCE(used_num, 0) - 1 ELSE 0 END"));
    }
}
