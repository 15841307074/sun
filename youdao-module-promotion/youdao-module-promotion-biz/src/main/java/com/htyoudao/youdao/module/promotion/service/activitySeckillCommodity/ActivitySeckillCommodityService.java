package com.htyoudao.youdao.module.promotion.service.activitySeckillCommodity;

import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;

import java.util.List;

public interface ActivitySeckillCommodityService {
    void createBatch(List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS);

    void updateBatch(List<ActivitySeckillCommodityDO> activitySeckillCommodityDOS);

    List<ActivitySeckillCommodityDO> selectByActivityId(Long activityId);


    void deleteByActivityId(Long id);


    void updateCommodityInfo(ActivityCommodityDTO activityCommodityDTO);
}
