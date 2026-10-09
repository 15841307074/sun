package com.htyoudao.youdao.module.promotion.service.activitySeckillTime;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillTime.ActivitySeckillTimeDO;

import java.util.List;

public interface ActivitySeckillTimeService {
    void createBatch(List<ActivitySeckillTimeDO> activitySeckillTimeDOS);

    void updateBatch(List<ActivitySeckillTimeDO> activitySeckillTimeDOS);

    List<ActivitySeckillTimeDO> selectByActivityId(Long activityId);

    void deleteByActivityId(Long id);
}
